package com.shocktrade.server.trading

import java.util.Date

import com.ldaniels528.tabular.Tabular
import com.ldaniels528.tabular.formatters.FormatHandler
import com.shocktrade.models.contest.{Contest, OrderType, PriceType}
import com.shocktrade.services.googlefinance.GoogleFinanceGetPricesService
import com.shocktrade.services.googlefinance.GoogleFinanceGetPricesService.{GfGetPricesRequest, GfPriceQuote}
import com.shocktrade.services.util.DateUtil._
import com.shocktrade.util.{DateUtil, ConcurrentCache}
import org.joda.time.DateTime
import play.api.Logger
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Trading Order Processor
 * @author lawrence.daniels@gmail.com
 */
object OrderProcessor {
  private val tabular = new Tabular().add(BSONObjectIDHandler)
  private val daysCloseLabels = Map(false -> "Open", true -> "Closed")
  private val quoteCache = ConcurrentCache[String, Seq[GfPriceQuote]](2.hours)

  def isTradingActive(asOfDate: Date) = DateUtil.isTradingActive(asOfDate)

  /**
   * Processes the given contest
   */
  def processContest(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext) = {
    // compute the market close time (+20 minutes for margin of error)
    val tradingClose = new DateTime(getTradeStopTime()).plusMinutes(20).toDate

    // perform the claiming process
    val results = {
      // if claiming has never occurred, serially execute each
      if (c.processedTime.isEmpty) processOrders(c, asOfDate, isDaysClose = isTradingActive(asOfDate))

      // was the last run while trading still active?
      else if (c.processedTime.isEmpty || c.processedTime.exists(_ < tradingClose)) processOrders(c, asOfDate, isDaysClose = false)

      // if not, was there already an end-of-Trading Day-event?
      else if (c.lastMarketClose.isEmpty || c.lastMarketClose.exists(_ < tradingClose)) processOrders(c, tradingClose, isDaysClose = true)

      // otherwise nothing happens
      else Future.successful(Nil)

      /*
      for {
        task1 <- processOrders(c, asOfDate, isDaysClose = false)
        task2 <- processOrders(c, asOfDate, isDaysClose = true)
      } yield task1 ++ task2*/
    }

    // gather the outcomes
    val allOutcomes = for {
    // unwrap the claiming outcomes
      claimOutcomes <- results

      // close any expired orders
      closeOrders <- ContestDAO.closeExpiredOrders(c, asOfDate)

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
    info(c, s"Processing Orders as of ${new DateTime(asOfDate).toString("MM/dd/yyyy hh:mm:ss")} [Market ${daysCloseLabels(isDaysClose)}]")

    // if it's day's close, grab all orders; otherwise, all non-Market Close orders
    val openOrders = ContestDAO.getOpenWorkOrders(c, asOfDate)
    val orders = if (isDaysClose) openOrders else openOrders.filterNot(_.priceType == PriceType.MARKET_ON_CLOSE)

    info(c, s"${orders.size} eligible order(s) found")
    tabular.transform(orders) foreach (info(c, _))

    // get the common asset quotes
    val quotes = getEligibleStockQuotes(c, orders, asOfDate)

    // process the market close orders
    val eligibleClaims = orders flatMap (processOrder(c, _, asOfDate, quotes))

    // display the eligible claims
    info(c, s"Attempting fulfillment on ${eligibleClaims.size} eligible claim(s)")
    tabular.transform(eligibleClaims) foreach (info(c, _))

    // update positions for processed orders
    processClaims(c, asOfDate, eligibleClaims)
  }

  private def processOrder(c: Contest, wo: WorkOrder, asOfDate: Date, quoteMap: Seq[StockQuote]): Option[Claim] = {
    for {
    // get the quotes for the order
    // attempt to find an eligible quote w/price
      quote <- quoteMap.find(q => q.symbol == wo.symbol && isEligible(c, wo, q))

    // create the claim
    } yield Claim(wo.symbol, wo.exchange, quote.price, wo.quantity, wo.commission, asOfDate, wo)
  }

  private def processClaims(c: Contest, asOfDate: Date, claims: Seq[Claim])(implicit ec: ExecutionContext) = {
    Future.sequence(claims map { claim =>
      for {
      // perform the BUY or SELL
        outcome <- claim.workOrder.orderType match {
          case OrderType.BUY =>
            info(c, s"[${claim.workOrder.playerId.stringify}] Increasing position of ${claim.symbol} x ${claim.quantity}")
            ContestDAO.increasePosition(c, claim, asOfDate)
          case OrderType.SELL =>
            info(c, s"[${claim.workOrder.playerId}] Reducing position of ${claim.symbol} x ${claim.quantity}")
            ContestDAO.reducePosition(c, claim, asOfDate)
          case orderType =>
            error(c, s"[${claim.workOrder.playerId.stringify}] position of ${claim.symbol} x ${claim.quantity} - Unrecognized order type $orderType")
            throw new IllegalStateException(s"Unrecognized order type $orderType")
        }
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
        if (outcome > 0)
          info(c, s"Order #${claim.workOrder.id}: count = $outcome")
    }
  }

  private def isEligible(c: Contest, wo: WorkOrder, q: StockQuote) = {
    // is it a valid claim?
    val requiredVolume = wo.quantity + (if (wo.orderTime < getTradeStartTime) 0L else wo.volumeAtOrderTime)
    val goodTimeAndVolume = (wo.orderTime <= q.tradeDateTime) && (q.totalVolume >= requiredVolume)
    val goodPrice = wo.price map (limit => if (wo.orderType == OrderType.BUY) limit >= q.price else limit <= q.price)

    // if the requied volume is satified, and
    // the price type is either not LIMIT or the price is satisfied, then claim it
    info(c, s"::isEligible => ${q.symbol} requiredVolume = $requiredVolume, goodTimeAndVolume = $goodTimeAndVolume, goodPrice = $goodPrice")
    goodTimeAndVolume && (wo.priceType != PriceType.LIMIT || goodPrice.contains(true))
  }

  private def getEligibleStockQuotes(c: Contest, orders: Seq[WorkOrder], asOfDate: Date): Seq[StockQuote] = {
    // get the distinct set of symbols we need
    val symbols = orders.map(_.symbol).distinct

    // attempt to retrieve as many quotes from cache as we can
    val cachedQuotes = Map((for {
      symbol <- symbols
      prices <- quoteCache.get(symbol)
    } yield (symbol, prices)): _*)

    info(c, s"${cachedQuotes.size} quote(s) retrieved from cache")

    // retrieve all remaining pricing quotes from the service
    val svcQuotes = Map(symbols.filterNot(cachedQuotes.contains) map { symbol =>
      (symbol, GoogleFinanceGetPricesService.getQuotes(GfGetPricesRequest(symbol, intervalInSecs = 5, periodInDays = 1)))
    }: _*)

    info(c, s"${svcQuotes.size} quote(s) retrieved from the service layer")

    // store the service quote in the cache
    svcQuotes.foreach { case (symbol, prices) => quoteCache.put(symbol, prices) }
    tabular.transform(svcQuotes.values.toSeq.flatten) foreach (info(c, _))

    // combine the service and cached quotes
    val quotes = svcQuotes ++ cachedQuotes

    // build the collection of stock quotes for claiming
    val stockQuotes = (orders flatMap { o =>
      quotes.get(o.symbol) map { prices =>
        val totalVolume = prices.map(_.volume).sum
        prices map (p => StockQuote(o.symbol, o.exchange, p.close, p.time, p.volume, totalVolume))
      }
    }).flatten

    // display the stock quotes
    stockQuotes
  }

  private def info(c: Contest, message: String) = Logger.info(s"${c.name}: $message")

  private def error(c: Contest, message: String, e: Throwable = null) = Logger.error(s"${c.name}: $message", e)

  /**
   * BSON Object ID Handler for Tabular
   */
  object BSONObjectIDHandler extends FormatHandler {
    override def handles(value: Any): Boolean = value.isInstanceOf[BSONObjectID]

    override def format(value: Any): Option[String] = value match {
      case _id: BSONObjectID => Some(_id.stringify)
      case _ => None
    }
  }

  /**
   * Generically represents the common elements of a stock quote
   */
  case class StockQuote(symbol: String,
                        exchange: String,
                        price: Double,
                        tradeDateTime: Date,
                        volume: Long,
                        totalVolume: Long)

}
