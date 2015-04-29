package com.shocktrade.server

import java.net.InetAddress
import java.util.Date

import com.shocktrade.models.contest.{Contest, OrderType, PriceType}
import com.shocktrade.server.TradingDAO.{Claim, WorkOrder}
import com.shocktrade.services.util.DateUtil._
import com.shocktrade.services.util.Tabular
import com.shocktrade.services.{NASDAQIntraDayQuotesService, YahooFinanceServices}
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.Try

/**
 * Contest Processor
 * @author lawrence.daniels@gmail.com
 */
object ContestProcessor {
  private val tabular = new Tabular()
  private val intraDayQuoteSvc = new NASDAQIntraDayQuotesService()

  /**
   * Processes the given contest
   */
  def processContest(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext): Future[Int] = {
    val tradingClose = new DateTime(getTradeStopTime()).plusMinutes(20).toDate

    // perform claiming
    val results = {
      // was the last run while trading still active?
      if (c.processedTime.exists(_ < tradingClose)) processOrders(c, asOfDate, isDaysClose = false)

      // if not, was there already an end-of-Trading Day-event?
      else if (c.lastMarketClose.exists(_ < tradingClose)) processOrders(c, tradingClose, isDaysClose = true)

      // nothing happens
      else promiseSuccess(Seq.empty)
    }

    // gather the outcomes
    val allOutcomes = for {
    // unwrap the claiming outcomes
      claimOutcomes <- results

      // close any expired orders
      closeOrders <- TradingDAO.closeExpiredOrders(c, asOfDate)

      // compute the updated count
      updatedCount = (claimOutcomes map { case (_, oc) => oc } sum) + closeOrders

    } yield (claimOutcomes, closeOrders, updatedCount)

    // if successful, display the summary,
    // and return the update count
    allOutcomes map {
      case (claimOutcomes, closeOrders, updatedCount) =>
        showProcessingSummary(c, claimOutcomes, closeOrders, updatedCount)
        updatedCount
    }
  }

  /**
   * Processes the given orders
   */
  private def processOrders(c: Contest, asOfDate: Date, isDaysClose: Boolean)(implicit ec: ExecutionContext) = {
    info(c, s"Processing Orders as of ${new DateTime(asOfDate).toString("MM/dd/yyyy hh:mm:ss")}")

    // if it's day's close, grab all orders; otherwise, all non-Market Close orders
    val allOrders = TradingDAO.getOpenWorkOrders(c, asOfDate)
    val orders = if (isDaysClose) allOrders else allOrders filterNot (_.priceType == PriceType.MARKET_ON_CLOSE)
    tabular.transform(orders) foreach (info(c, _))

    // process the orders
    for {
    // get the common asset quotes
      quotes <- getStockQuotes(c, orders, asOfDate)

      // process the market close orders
      claims = orders flatMap (processOrder(_, asOfDate, quotes))

      _ = {
        info(c, "Eligible claims:")
        tabular.transform(claims) foreach Logger.info

        quotes foreach {
          case (k, v) =>
            tabular.transform(v) foreach Logger.info
        }
      }

      // update positions for processed orders
      ordersAndPositions <- updateOrdersAndPositions(c, asOfDate, claims)
    } yield ordersAndPositions
  }

  private def processOrder(wo: WorkOrder, asOfDate: Date, quoteMap: Map[String, Seq[StockQuote]]): Option[Claim] = {
    for {
    // get the quotes for the order
      quotes <- quoteMap.get(wo.symbol)

      // attempt to find an eligible quote w/price
      quote <- quotes.find(isEligible(wo, _).contains(true))
      price <- quote.price

    // create the claim
    } yield Claim(wo.symbol, wo.exchange, price, wo.quantity, wo.commission, asOfDate, wo)
  }

  private def updateOrdersAndPositions(c: Contest, asOfDate: Date, claims: Seq[Claim]) = {
    Future.sequence(claims map { claim =>
      for {
      // perform the BUY or SELL
        outcome <- if (claim.workOrder.orderType == OrderType.BUY)
          TradingDAO.increasePosition(c, claim, asOfDate)
        else
          TradingDAO.reducePosition(c, claim, asOfDate)
      } yield (claim, outcome)
    })
  }

  private def showProcessingSummary(c: Contest, outcomes: Seq[(Claim, Int)], closedOrders: Int, updatedCount: Int) = {
    // display the summary information
    info(c, s"$updatedCount claimed of ${outcomes.length} qualified order(s)")

    // close orders
    if (closedOrders > 0) info(c, s"$closedOrders order(s) closed")

    // display the failures
    outcomes foreach {
      case (claim, outcome) =>
        info(c, s"Order #${claim.workOrder._id}: count = $outcome")
    }
  }

  private def isEligible(wo: WorkOrder, q: StockQuote) = {
    for {
    // get the quote attributes
      price <- q.price
      tradeDateTime <- q.tradeDateTime
      volume <- q.volume

      // is it a valid claim?
      requiredVolume = wo.quantity + (if (wo.orderTime < getTradeStartTime) 0d else wo.volumeAtOrderTime)
      goodTimeAndVolume = (wo.orderTime <= tradeDateTime) && (volume >= requiredVolume)
      goodPrice = wo.limitPrice map (limit => if (wo.orderType == OrderType.BUY) limit >= price else limit <= price)

    } yield (wo.priceType != PriceType.LIMIT || goodPrice.contains(true)) && goodTimeAndVolume
  }

  private def getStockQuotes(c: Contest, orders: Seq[WorkOrder], asOfDate: Date)(implicit ec: ExecutionContext): Future[Map[String, Seq[StockQuote]]] = {
    import com.shocktrade.services.NASDAQIntraDayQuotesService._

    // get the sequence of intra-day quotes
    Future.sequence(orders map { o =>
      // determine the starting index of the time slot
      val startIndex = ET_0930_TO_0959
      //c.lastRunTime map (getTimeIndex(_)) getOrElse ET_0930_TO_0959
      val endIndex = NASDAQIntraDayQuotesService.getTimeSlot(asOfDate)

      // start the tasks so that they execute simultaneously
      val intraDayQuotes = intraDayQuoteSvc.getQuotesInRange(o.symbol, startIndex, endIndex)
      val realTimeQuote = YahooFinanceServices.getRealTimeStockQuote(o.symbol)

      for {
      // retrieve the quotes for the instance in time
        quotes <- intraDayQuotes

        // compute the volume (since the order was placed)
        volume <- realTimeQuote map (_.volume)

        // transform the intra-day quotes into "common" stock quotes
        stockQuotes = {
          var aggregateVolume = 0L
          quotes map { q =>
            q.volume foreach (aggregateVolume += _)
            StockQuote(q.symbol, o.exchange, q.price, q.tradeDateTime, volume, Some(aggregateVolume))
          }
        }

      // filter the quotes for only the ones we're interested in
      } yield stockQuotes filter (_.tradeDateTime.exists(_ >= o.orderTime))
    }) map (_.flatten groupBy (_.symbol))
  }

  private def info(c: Contest, message: String) = {
    Logger.info(s"${c.name}: $message")
  }

  private def error(c: Contest, message: String, e: Throwable = null) = {
    Logger.error(s"${c.name}: $message", e)
  }

  /**
   * Returns the name of the host that the process is running on
   */
  private def getHostName = Try(InetAddress.getLocalHost.getHostName).toOption.orNull

  /**
   * Returns a fulfilled promise of a successful result
   */
  private def promiseSuccess[S](result: S): Future[S] = {
    val promise = Promise[S]()
    promise.success(result)
    promise.future
  }

  /**
   * Returns a fulfilled promise of a failure result
   */
  private def promiseFailure[S](cause: Throwable): Future[S] = {
    val promise = Promise[S]()
    promise.failure(cause)
    promise.future
  }

  /**
   * Generically represents the common elements of a stock quote
   */
  case class StockQuote(symbol: String,
                        exchange: String,
                        price: Option[Double],
                        tradeDateTime: Option[Date],
                        volume: Option[Long],
                        aggregateVolume: Option[Long])

}
