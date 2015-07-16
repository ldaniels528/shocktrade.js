package com.shocktrade.server.trading

import java.util.Date

import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.tabular.Tabular
import com.ldaniels528.tabular.formatters.FormatHandler
import com.shocktrade.models.contest.OrderTerms.OrderTerm
import com.shocktrade.models.contest.PriceTypes.PriceType
import com.shocktrade.models.contest._
import com.shocktrade.models.profile.{UserProfileDAO, UserProfiles}
import com.shocktrade.server.trading.Outcome.{Failed, Succeeded}
import com.shocktrade.services.util.DateUtil._
import com.shocktrade.services.yahoofinance.YFIntraDayQuotesService.YFIntraDayQuote
import com.shocktrade.services.yahoofinance.{YFIntraDayQuotesService, YFStockQuoteService}
import com.shocktrade.util.{ConcurrentCache, DateUtil}
import org.joda.time.DateTime
import play.api.Logger
import play.libs.Akka
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
  private implicit val ec = Akka.system().dispatcher

  /**
   * Processes the given contest
   */
  def processContest(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext): Future[Int] = {
    // compute the market close time (+20 minutes for margin of error)
    val tradingClose = new DateTime(getTradeStopTime()).plusMinutes(20).toDate

    // gather the outcomes
    val allOutcomes = for {
    // unwrap the claiming outcomes
      claimOutcomes <- processOrders(c, asOfDate, isDaysClose = isTradingActive(asOfDate))

      // close any expired orders
      closeOrders <- ContestDAO.closeExpiredOrders(c, asOfDate)

      // compute the updated count
      updatedCount = (claimOutcomes map { case (_, outcome) => if (outcome.isSuccess) 1 else 0 } sum) + closeOrders

    } yield (claimOutcomes, closeOrders, updatedCount)

    // is this contest expired?
    if (c.expirationTime.exists(expTime => tradingClose >= expTime)) {
      closeContest(c, asOfDate)
    }

    // if successful, display the summary,
    // and return the update count
    allOutcomes map {
      case (claimOutcomes, closeOrders, updatedCount) =>
        showProcessingSummary(c, claimOutcomes, closeOrders, updatedCount)
        updatedCount
    }
  }

  def closeContest(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext) = {
    Logger.info(s"Closing contest '${c.name}' ...")

    val outcome = for {
      closeOrders <- closeAllOpenOrders(c)
      prices <- priceAllHeldSecurities(c)
      sellOff <- liquidateAllHeldSecurities(c, prices, asOfDate)
      closedContest <- ContestDAO.closeContest(c) map (_ orDie "Contest could not be closed")
      refunds <- refundTheProceedsToParticipants(closedContest)
      awards <- applyAwards(closedContest)
    } yield (sellOff, closedContest)

    // display the results
    outcome.map { case (sellOff, _) =>
      Logger.info(s"Contest '${c.name}' is closed.")
      tabular.transform(sellOff) foreach (info(c, _))
    }
    outcome
  }

  private def applyAwards(c: Contest)(implicit ec: ExecutionContext) = {
    Future.sequence {
      AwardsProcessor.qualifyAwards(c) map { case (participant, awards) =>
        UserProfileDAO.applyAwards(participant.id, awards)
      }
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
          participant = participant,
          accountType = pos.accountType,
          symbol = pos.symbol,
          exchange = pos.exchange,
          orderTime = asOfDate,
          orderTerm = OrderTerms.GOOD_FOR_DAY,
          orderType = OrderTypes.SELL,
          price = for {q <- quotes.get(pos.symbol); p <- q.lastTrade} yield p,
          priceType = PriceTypes.MARKET,
          quantity = pos.quantity,
          commission = Commissions.getCommission(PriceTypes.MARKET),
          emailNotify = true,
          partialFulfillment = false,
          marginAccount = participant.marginAccount
        )

        // create a fake claim
        val claim = Claim(
          symbol = pos.symbol,
          exchange = pos.exchange,
          price = pos.pricePaid,
          quantity = pos.quantity,
          commission = Commissions.getCommission(PriceTypes.MARKET),
          purchaseTime = asOfDate,
          workOrder = workOrder)

        // liquidate the asset
        ContestDAO.reducePosition(c, claim, asOfDate) map { outcome =>
          Liquidation(participant.name, pos.symbol, pos.pricePaid, pos.quantity, workOrder.price, outcome)
        }
      }
    })
  }

  private def refundTheProceedsToParticipants(c: Contest)(implicit ec: ExecutionContext) = {
    Future.sequence(c.participants map { participant =>
      val totalCash = participant.cashAccount.cashFunds + participant.marginAccount.map(_.cashFunds).getOrElse(BigDecimal(0.0))
      UserProfiles.deductFunds(participant.id, -totalCash)
    })
  }

  /**
   * Processes the given orders
   */
  private def processOrders(c: Contest, asOfDate: Date, isDaysClose: Boolean)(implicit ec: ExecutionContext) = {
    info(c, s"Processing Orders as of ${new DateTime(asOfDate).toString("MM/dd/yyyy hh:mm:ss")} [Market ${daysCloseLabels(isDaysClose)}]")

    // if it's day's close, grab all orders; otherwise, all non-Market Close orders
    val orders = ContestDAO.getOpenWorkOrders(c, asOfDate)
    if (orders.nonEmpty) {
      info(c, s"${orders.size} eligible order(s) found")
      showOrders(c, orders)

      // get the common asset quotes
      val quotes = getEligibleStockQuotes(c, orders, asOfDate)

      // process the market close orders
      val eligibleClaims = orders flatMap { o =>
        getEligibleClaim(c, o, asOfDate, quotes.filter(_.symbol == o.symbol))
      }

      // display the eligible claims
      info(c, s"Attempting fulfillment on ${eligibleClaims.size} eligible claim(s)")
      showEligibleClaims(c, eligibleClaims)

      // update positions for processed orders
      val outcomes = fulfillOrders(c, asOfDate, eligibleClaims)
      showOrderFulfillment(c, outcomes)
      outcomes
    }
    else Future.successful(Nil)
  }

  private def showOrders(c: Contest, orders: Seq[WorkOrder]): Unit = {
    tabular.transform(orders map { o =>
      DisplayOrder(o.participant.name, o.symbol, o.priceType, o.price, o.quantity, o.orderTime, o.orderTerm)
    }) foreach (info(c, _))
  }

  case class DisplayOrder(player: String, symbol: String, priceType: PriceType, price: Option[BigDecimal], quantity: BigDecimal, time: Date, term: OrderTerm)

  private def showEligibleClaims(c: Contest, eligibleClaims: Seq[Claim]) {
    tabular.transform(eligibleClaims.map { ec =>
      DisplayEligibleClaim(ec.workOrder.participant.name, ec.symbol, ec.price, ec.quantity, ec.purchaseTime, ec.workOrder.orderTerm)
    }) foreach (info(c, _))
  }

  case class DisplayEligibleClaim(player: String, symbol: String, price: BigDecimal, quantity: BigDecimal, time: Date, term: OrderTerm)

  private def showOrderFulfillment(c: Contest, result: Future[Seq[(Claim, Outcome)]]) {
    result.map {
      _ map { case (claim, o) =>
        DisplayClaimOutcome(claim.workOrder.participant.name, claim.symbol, claim.price, claim.quantity, claim.purchaseTime, o match {
          case Succeeded(n) => "Claimed"
          case Failed(message, _) => message
        })
      }
    } foreach { outcomes =>
      tabular.transform(outcomes) foreach (info(c, _))
    }
  }

  case class DisplayClaimOutcome(player: String, symbol: String, price: BigDecimal, quantity: BigDecimal, time: Date, status: String)

  private def getEligibleClaim(c: Contest, wo: WorkOrder, asOfDate: Date, quotes: Seq[WorkQuote]): Option[Claim] = {
    var qualifications: List[DisplayQualification] = Nil

    // generate the list of potential claims
    val potentials = quotes.foldLeft[List[Claim]](Nil) { (list, q) =>
      // is it a valid claim?
      val (isGoodTime, time) = isEligibleTime(c, wo, q, asOfDate)
      val (isGoodVolume, quantity) = isEligibleVolume(c, wo, q)
      val (isGoodPrice, price) = isEligiblePrice(c, wo, q)

      qualifications = DisplayQualification(wo.symbol, wo.priceType, time, isGoodTime, price, isGoodPrice, quantity, isGoodVolume) :: qualifications

      // if the required volume is satisfied, and
      // the price type is either not LIMIT or the price is satisfied, then claim it
      if (isGoodTime && isGoodVolume && isGoodPrice)
        Claim(
          symbol = wo.symbol,
          exchange = wo.exchange,
          price = price,
          quantity = quantity,
          commission = wo.commission,
          purchaseTime = time,
          workOrder = wo) :: list
      else
        list
    }

    //info(c, "Qualifications:")
    //tabular.transform(qualifications) foreach(info(c, _))

    // one claim per work order ID,
    // produce the lowest price or the highest quantity
    val claim =
      if (wo.partialFulfillment)
        potentials.sortBy(-_.quantity).headOption
      else
        potentials.sortBy(_.price).headOption

    // if we can claim a higher quantity ...
    // remember we'll have to dollar-cost average the prices
    if (potentials.nonEmpty && (wo.quantity >= potentials.map(_.quantity).sum))
      dollarCostAverage(potentials)
    else
      claim
  }

  case class DisplayQualification(symbol: String, priceType: PriceType, time: Date, isGoodTime: Boolean, price: Double, isGoodPrice: Boolean, quantity: Long, isGoodVolume: Boolean)

  private def dollarCostAverage(potentials: Seq[Claim]) = {
    val totalQuantity = potentials.map(_.quantity).sum
    val totalCost = potentials.map(c => c.price * c.quantity.toDouble).sum
    val averagePrice = totalCost / totalQuantity.toDouble
    potentials.headOption.map(_.copy(price = averagePrice, quantity = totalQuantity))
  }

  private def showProcessingSummary(c: Contest, outcomes: Seq[(Claim, Outcome)], closedOrders: Int, updatedCount: Int) = {
    // display the summary information
    info(c, s"$updatedCount claimed of ${outcomes.length} qualified order(s)")

    // close orders
    if (closedOrders > 0) info(c, s"$closedOrders order(s) closed")

    // display the failures
    outcomes foreach {
      case (claim, outcome) =>
        info(c, s"Order #${claim.workOrder.id}: outcome = $outcome")
    }
  }

  private def isEligiblePrice(c: Contest, wo: WorkOrder, q: WorkQuote): (Boolean, Double) = {
    val isGood = wo.priceType match {
      case PriceTypes.LIMIT =>
        wo.orderType match {
          case OrderTypes.BUY => wo.price exists (q.price <= _)
          case OrderTypes.SELL => wo.price exists (q.price >= _)
        }
      case PriceTypes.STOP_LIMIT =>
        wo.orderType match {
          case OrderTypes.BUY => wo.price exists (q.price <= _)
          case OrderTypes.SELL => wo.price exists (q.price >= _)
        }
      case _ => true
    }
    ((q.price > 0.00d) && isGood, q.price)
  }

  private def isEligibleTime(c: Contest, wo: WorkOrder, q: WorkQuote, asOfDate: Date): (Boolean, Date) = {
    val isGood = wo.priceType match {
      case PriceTypes.MARKET_ON_CLOSE => !TradingClock.isTradingActive(asOfDate) || asOfDate > DateUtil.getTradeStopTime(asOfDate)
      case _ => wo.orderTime <= q.tradeDateTime
    }
    (isGood, q.tradeDateTime)
  }

  private def isEligibleVolume(c: Contest, wo: WorkOrder, q: WorkQuote): (Boolean, Long) = {
    if (q.totalVolume >= wo.quantity) (true, wo.quantity) else (wo.partialFulfillment && q.totalVolume > 0, q.totalVolume)
  }

  private def getEligibleStockQuotes(c: Contest, orders: Seq[WorkOrder], asOfDate: Date): Seq[WorkQuote] = {
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
    //tabular.transform(svcQuotes.values.toSeq.flatten) foreach (info(c, _))

    // combine the service and cached quotes
    val quotes = svcQuotes ++ cachedQuotes

    // build the collection of stock quotes for claiming
    val stockQuotes = (orders flatMap { o =>
      quotes.get(o.symbol) map { prices =>
        val totalVolume = prices.map(_.volume).sum
        prices map (p => WorkQuote(o.symbol, o.exchange, p.close, p.timestamp, p.volume, totalVolume))
      }
    }).flatten

    // display the stock quotes
    stockQuotes
  }

  private def fulfillOrders(c: Contest, asOfDate: Date, claims: Seq[Claim])(implicit ec: ExecutionContext) = {
    Future.sequence(claims map { claim =>
      for {
      // perform the BUY or SELL
        outcome <- claim.workOrder.orderType match {
          case OrderTypes.BUY =>
            info(c, s"[${claim.workOrder.playerId}] Increasing position of ${claim.symbol} x ${claim.quantity}")
            ContestDAO.increasePosition(c, claim, asOfDate) map {
              case o@Failed(error, _) if error.contains("qualifying position") =>
                if (!claim.workOrder.participant.perks.contains(PerkTypes.PRFCTIMG)) {
                  ContestDAO.failOrder(c, claim.workOrder, error, asOfDate)
                }
                o
              case outcome => outcome
            }
          case OrderTypes.SELL =>
            info(c, s"[${claim.workOrder.playerId}] Reducing position of ${claim.symbol} x ${claim.quantity}")
            ContestDAO.reducePosition(c, claim, asOfDate) map {
              case o@Failed(error, _) if error.contains("qualifying position") =>
                if (!claim.workOrder.participant.perks.contains(PerkTypes.PRCHEMNT)) {
                  ContestDAO.failOrder(c, claim.workOrder, error, asOfDate)
                }
                o
              case outcome => outcome
            }
        }
      } yield (claim, outcome)
    })
  }

  private def isTradingActive(asOfDate: Date) = DateUtil.isTradingActive(asOfDate)

  private def info(c: Contest, message: String) = Logger.info(s"${c.name}: $message")

  private def error(c: Contest, message: String, e: Throwable = null) = Logger.error(s"${c.name}: $message", e)

  /**
   * BSON Object ID Handler for Tabular
   * @author lawrence.daniels@gmail.com
   */
  object BSONObjectIDHandler extends FormatHandler {
    override def handles(value: Any) = value match {
      case id: BSONObjectID => true
      case _ => false
    }

    override def format(value: Any): Option[String] = value match {
      case _id: BSONObjectID => Some(_id.stringify)
      case _ => None
    }
  }

  case class Liquidation(player: String, symbol: String, pricePaid: BigDecimal, quantity: Long, marketPrice: Option[BigDecimal], outcome: Outcome)

  case class Pricing(symbol: String, exchange: Option[String], lastTrade: Option[Double], tradeDate: Option[Date])

  /**
   * Generically represents the common elements of a stock quote
   * @author lawrence.daniels@gmail.com
   */
  case class WorkQuote(symbol: String,
                       exchange: String,
                       price: Double,
                       tradeDateTime: Date,
                       volume: Long,
                       totalVolume: Long)

}
