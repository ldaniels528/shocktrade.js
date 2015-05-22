package com.shocktrade.server.trading

import java.text.SimpleDateFormat
import java.util.Date

import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.tabular.Tabular
import com.ldaniels528.tabular.formatters.FormatHandler
import com.shocktrade.models.contest.{Commissions, Contest, OrderType, PriceType}
import com.shocktrade.models.profile.UserProfiles
import com.shocktrade.services.util.DateUtil._
import com.shocktrade.services.yahoofinance.YFIntraDayQuotesService.YFIntraDayQuote
import com.shocktrade.services.yahoofinance.{YFIntraDayQuotesService, YFStockQuoteService}
import com.shocktrade.util.{ConcurrentCache, DateUtil}
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
  private val quoteCache = ConcurrentCache[String, Seq[YFIntraDayQuote]](2.hours)

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

    // is this contest expired?
    if (c.expirationTime.exists(expTime => tradingClose >= expTime)) {
      closedExpiredContest(c, asOfDate)
    }

    // if successful, display the summary,
    // and return the update count
    allOutcomes map {
      case (claimOutcomes, closeOrders, updatedCount) =>
        showProcessingSummary(c, claimOutcomes, closeOrders, updatedCount)
        updatedCount
    }
  }

  def closedExpiredContest(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext) = {
    Logger.info(s"Closing contest '${c.name}' ...")

    val outcome = for {
      closeOrders <- closeAllOpenOrders(c)
      prices <- priceAllHeldSecurities(c)
      sellOff <- liquidateAllHeldSecurities(c, prices, asOfDate)
      closedContest <- ContestDAO.closeContest(c) map (_ orDie "Contest could not be closed")
      refunds <- refundTheProceedsToParticipants(closedContest)
    } yield (sellOff, closedContest)

    // display the results
    outcome.map { case (sellOff, _) =>
      Logger.info(s"Contest '${c.name}' is closed.")
      tabular.transform(sellOff) foreach (info(c, _))
    }
  }

  private def closeAllOpenOrders(c: Contest)(implicit ec: ExecutionContext) = {
    info(c, "[1] Close all active orders")
    Future.sequence(c.participants.flatMap(participant => participant.orders map (order => ContestDAO.closeOrder(c.id, participant.id, order.id))))
  }

  private def priceAllHeldSecurities(c: Contest)(implicit ec: ExecutionContext) = {
    info(c, "[2] Price all currently held securities")
    val parameters = YFStockQuoteService.getParams("symbol", "exchange", "lastTrade", "tradeDate")
    val tickers = c.participants.flatMap(_.positions.map(_.symbol)).distinct
    Future.sequence(tickers.sliding(32, 32) map { symbols =>
      val quotes = YFStockQuoteService.getQuotes(symbols, parameters)
      quotes.map(_ map (q => Pricing(q.symbol, q.exchange, q.lastTrade, q.tradeDate)))
    } toSeq) map (_.flatten)
  }

  private def liquidateAllHeldSecurities(c: Contest, prices: Seq[Pricing], asOfDate: Date)(implicit ec: ExecutionContext) = {
    info(c, "[3] Liquidate all currently held securities")

    // build the mapping of all prices
    val quotes = Map(prices.map(q => (q.symbol, q)): _*)
    //tabular.transform(prices) foreach (info(c, _))

    // sell-off all positions
    Future.sequence(c.participants flatMap { participant =>
      participant.positions map { pos =>
        // create a fake work-order
        val workOrder = WorkOrder(
          id = pos.id,
          playerId = participant.id,
          symbol = pos.symbol,
          exchange = pos.exchange,
          orderTime = asOfDate,
          expirationTime = None,
          orderType = OrderType.SELL,
          price = for {q <- quotes.get(pos.symbol); p <- q.lastTrade} yield p,
          priceType = PriceType.MARKET,
          quantity = pos.quantity,
          commission = Commissions.forMarket,
          emailNotify = true,
          volumeAtOrderTime = 0
        )

        // create a fake claim
        val claim = Claim(
          symbol = pos.symbol,
          exchange = pos.exchange,
          price = pos.pricePaid,
          quantity = pos.quantity,
          commission = Commissions.forMarket,
          purchaseTime = asOfDate,
          workOrder = workOrder)

        // liquidate the asset
        ContestDAO.reducePosition(c, claim, asOfDate) map { update =>
          Liquidation(participant.name, pos.symbol, pos.pricePaid, pos.quantity, workOrder.price, update)
        }
      }
    })
  }

  private def refundTheProceedsToParticipants(c: Contest)(implicit ec: ExecutionContext) = {
    Future.sequence(c.participants map { participant =>
      UserProfiles.deductFunds(participant.id, -participant.fundsAvailable)
    })
  }

  case class Pricing(symbol: String, exchange: Option[String], lastTrade: Option[Double], tradeDate: Option[Date])

  case class Liquidation(player: String, symbol: String, pricePaid: BigDecimal, quantity: Int, marketPrice: Option[BigDecimal], update: Int = 0)

  /**
   * Processes the given orders
   */
  private def processOrders(c: Contest, asOfDate: Date, isDaysClose: Boolean)(implicit ec: ExecutionContext) = {
    info(c, s"Processing Orders as of ${new DateTime(asOfDate).toString("MM/dd/yyyy hh:mm:ss")} [Market ${daysCloseLabels(isDaysClose)}]")

    // if it's day's close, grab all orders; otherwise, all non-Market Close orders
    val openOrders = ContestDAO.getOpenWorkOrders(c, asOfDate)
    val orders = if (isDaysClose) openOrders else openOrders.filterNot(_.priceType == PriceType.MARKET_ON_CLOSE)

    if (orders.nonEmpty) {
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
    else Future.successful(Nil)
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

  private def isEligible(c: Contest, wo: WorkOrder, q: StockQuote): Boolean = {
    def isOk(state: Boolean) = if (state) "Ok" else "Bad"

    // is it a valid claim?
    val (isGoodTime, time) = isEligibleTime(c, wo, q)
    val (isGoodVolume, volume) = isEligibleVolume(c, wo, q)
    val (isGoodPrice, price) = isEligiblePrice(c, wo, q)

    // if the required volume is satisfied, and
    // the price type is either not LIMIT or the price is satisfied, then claim it
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    info(c, f"[${sdf.format(new Date)}] symbol ${q.symbol}, price $price%.04f [${isOk(isGoodPrice)}], volume: $volume [${isOk(isGoodVolume)}] time ${sdf.format(time)} [${isOk(isGoodTime)} - order: ${sdf.format(wo.orderTime)}]")

    isGoodTime && isGoodVolume && isGoodPrice
  }

  private def isEligibleTime(c: Contest, wo: WorkOrder, q: StockQuote): (Boolean, Date) = {
    (wo.orderTime <= q.tradeDateTime, q.tradeDateTime)
  }

  private def isEligibleVolume(c: Contest, wo: WorkOrder, q: StockQuote): (Boolean, Long) = {
    (q.totalVolume >= wo.quantity, wo.quantity)
  }

  private def isEligiblePrice(c: Contest, wo: WorkOrder, q: StockQuote): (Boolean, Double) = {
    wo.priceType match {
      case PriceType.MARKET => (q.price > 0, q.price)
      case PriceType.MARKET_ON_CLOSE => (q.price > 0, q.price)
      case PriceType.LIMIT =>
        val isGood = wo.price exists (limit => (q.price > 0) && ((wo.orderType == OrderType.BUY && limit >= q.price) || (limit <= q.price)))
        (isGood, q.price)
      case PriceType.STOP_LIMIT =>
        val isGood = wo.price exists (limit => (q.price > 0) && ((wo.orderType == OrderType.BUY && limit >= q.price) || (limit <= q.price))) // TODO
        (isGood, q.price)
      case priceType =>
        error(c, s"Unhandled price type - $priceType")
        (false, q.price)
    }
  }

  private def getEligibleStockQuotes(c: Contest, orders: Seq[WorkOrder], asOfDate: Date): Seq[StockQuote] = {
    // get the distinct set of symbols we need
    val symbols = orders.map(_.symbol).distinct
    info(c, s"Retrieving quotes for symbols: ${symbols.mkString(",")}")

    // attempt to retrieve as many quotes from cache as we can
    val cachedQuotes = Map((for {
      symbol <- symbols
      prices <- quoteCache.get(symbol)
    } yield (symbol, prices)): _*)

    info(c, s"${cachedQuotes.size} quote(s) retrieved from cache")

    // retrieve all remaining pricing quotes from the service
    val svcQuotes = Map(symbols.filterNot(cachedQuotes.contains) map { symbol =>
      (symbol, YFIntraDayQuotesService.getQuotes(symbol))
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
        prices map (p => StockQuote(o.symbol, o.exchange, p.close, p.timestamp, p.volume, totalVolume))
      }
    }).flatten

    // display the stock quotes
    stockQuotes
  }

  private def isTradingActive(asOfDate: Date) = DateUtil.isTradingActive(asOfDate)

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
