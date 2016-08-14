package com.shocktrade.server.tqm

import com.shocktrade.javascript.models.contest.{Order, Participant}
import com.shocktrade.server.tqm.TradingQualificationEngine._
import com.shocktrade.server.tqm.data.ContestDAO._
import com.shocktrade.server.tqm.data.{Claim, ContestData, WorkOrder}
import com.shocktrade.server.tqm.services.YahooFinanceCSVQuotesService
import com.shocktrade.server.tqm.services.YahooFinanceCSVQuotesService.YFCSVQuote
import org.scalajs.nodejs._
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.os.OS
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Trading Qualification Engine
  * @author lawrence.daniels@gmail.com
  */
class TradingQualificationEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
  // load modules
  private val os = OS()
  private val moment = Moment()

  // get DAO and service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams("symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume")
  private val contestDAO = dbFuture.flatMap(_.getContestDAO)

  def run(): Unit = {
    val outcome = for {
      contestOpt <- contestDAO.flatMap(_.findNext(processingHost = os.hostname(), updateDelay = 5.seconds))
      claims <- processOrders(contestOpt)
    } yield claims

    outcome onComplete {
      case Success(claims) =>
        console.log(s"${claims.size} claim(s) were created")
        console.log("Process completed successfully")
      case Failure(e) =>
        console.error(s"Failed to process contest: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def processOrders(contestOpt: Option[ContestData]) = {
    contestOpt match {
      case Some(contest) =>
        console.log(s"Processing contest '${contest.name}' (${contest._id})")

        // determine the as-of date
        val asOfTime = contest.lastUpdate.getOrElse(js.Date.now())
        console.log(s"as-of date: ${new js.Date(asOfTime)}")

        // attempt to find eligible orders
        val participantOrders = contest.findEligibleOrders(asOfTime)
        val orders = participantOrders.map(_._2)

        // display the orders
        showOrders(contest, participantOrders)

        // retrieve the quotes for the orders
        // and perform the qualification process
        for {
          quotes <- lookupOrderQuotes(contest, orders)
          workOrders = performQualification(contest, participantOrders, quotes)
          outcome <- fulfillOrders(workOrders)
        } yield outcome

      case None =>
        console.log("No eligible contest(s) found")
        Future.successful(Nil)
    }
  }

  private def fulfillOrders(workOrders: Seq[WorkOrder]) = {
    Future.sequence {
      workOrders map { workOrder =>
        if (workOrder.order.isBuyOrder)
          contestDAO.flatMap(_.insertPosition(workOrder))
        else
          contestDAO.flatMap(_.reducePosition(workOrder))
      }
    }
  }

  private def lookupOrderQuotes(contest: ContestData, orders: Seq[Order]) = {
    // retrieve the quotes for each symbol
    val symbols = orders.flatMap(_.symbol.toList).distinct
    val quotes = csvQuoteSvc.getQuotes(cvsQuoteParams, symbols)
    quotes foreach (showQuotes(contest, _))
    quotes
  }

  private def performQualification(contest: ContestData, participantOrders: Seq[(Participant, Order)], quotes: Seq[QuoteType]) = {
    val quoteMapping = js.Dictionary(quotes map (q => q.symbol -> q): _*)
    participantOrders flatMap { case (participant, order) =>
      for {
        contestID <- contest._id.toOption
        participantID <- participant._id.toOption
        symbol <- order.symbol.toOption
        quote <- quoteMapping.get(symbol)
        claim <- order.qualify(quote)
      } yield new WorkOrder(contestID, participantID, order, claim)
    }
  }

  private def showOrders(contest: ContestData, participantOrders: Seq[(Participant, Order)]) = {
    console.log(s"'${contest.name}' - ${participantOrders.size} eligible order(s):")
    participantOrders.zipWithIndex foreach { case ((p, o), n) =>
      console.log(s"[${n + 1}] ${p.name}: ${o.orderType} / ${o.symbol} @ ${o.price} x ${o.quantity} - ${o.priceType} <${o._id}>")
    }
    console.log("")
  }

  private def showQuotes(contest: ContestData, quotes: Seq[QuoteType]) = {
    console.log(s"'${contest.name}' - ${quotes.size} quote(s):")
    quotes.zipWithIndex foreach { case (q, n) =>
      console.log(f"[${n + 1}] ${q.exchange}/${q.symbol} ${q.lastTrade} ${q.tradeDateTime.map(_.getTime())} [${q.tradeDateTime}]")
    }
    console.log("")
  }

}

/**
  * Trading Qualification Engine Companion
  * @author lawrence.daniels@gmail.com
  */
object TradingQualificationEngine {
  type QuoteType = YFCSVQuote

  /**
    * Order Qualification Logic
    * @param order the given [[Order order]]
    */
  final implicit class OrderQualification(val order: Order) extends AnyVal {

    @inline
    def qualify(quote: QuoteType) = if (order.isBuyOrder) qualifyBuy(quote) else qualifySell(quote)

    @inline
    def qualifyBuy(quote: QuoteType) = {
      if (order.isLimitOrder) qualifyBuyAtLimit(quote).toOption
      else if (order.isMarketOrder) None
      else if (order.isMarketAtCloseOrder) None
      else None
    }

    @inline
    def qualifyBuyAtLimit(quote: QuoteType) = Try {
      val quantity = order.quantity orDie "Missing order quantity"
      val price = order.price orDie "Missing LIMIT price"
      val orderTime = order.creationTime orDie "Missing order creation time"

      val exchange = quote.exchange orDie "Missing symbol"
      val volume = quote.volume orDie "Missing volume"
      val lastTrade = quote.lastTrade orDie "Missing last trade"
      val tradeTime = quote.tradeDateTime.map(_.getTime()) orDie "Missing trade time"

      // If the volume is greater than the desired quantity
      // and the price is less than or equal to the limit price
      // and the transaction occurred AFTER the order was created
      if (volume < quantity) reject(s"insufficient volume (required: $quantity, actual: $volume)")
      else if (lastTrade > price) reject(f"price too high (required: $price%.05f, actual: $lastTrade%.05f)")
      else if (orderTime > tradeTime) reject(s"out of time bounds (required: $tradeTime, actual: $orderTime)")
      else new Claim(symbol = quote.symbol, exchange = exchange, price = lastTrade, quantity = quantity, asOfTime = tradeTime)
    }

    @inline
    def qualifySell(quote: QuoteType) = {
      if (order.isLimitOrder) qualifySellAtLimit(quote).toOption
      else if (order.isMarketOrder) None
      else if (order.isMarketAtCloseOrder) None
      else None
    }

    @inline
    def qualifySellAtLimit(quote: QuoteType) = Try {
      val quantity = order.quantity orDie "Missing order quantity"
      val price = order.price orDie "Missing LIMIT price"
      val orderTime = order.creationTime orDie "Missing order creation time"

      val exchange = quote.exchange orDie "Missing symbol"
      val volume = quote.volume orDie "Missing volume"
      val lastTrade = quote.lastTrade orDie "Missing last trade"
      val tradeTime = quote.tradeDateTime.map(_.getTime()) orDie "Missing trade time"

      // If the volume is greater than the desired quantity
      // and the price is less than or equal to the limit price
      // and the transaction occurred AFTER the order was created
      if (volume < quantity) reject(s"insufficient volume (required: $quantity, actual: $volume)")
      else if (lastTrade < price) reject(f"price too low (required: $price%.05f, actual: $lastTrade%.05f)")
      else if (orderTime > tradeTime) reject(s"out of time bounds (required: $tradeTime, actual: $orderTime)")
      else new Claim(symbol = quote.symbol, exchange = exchange, price = lastTrade, quantity = quantity, asOfTime = tradeTime)
    }

    @inline
    def reject(message: String) = die(s"Order # ${order._id}: $message")

  }

}