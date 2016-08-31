package com.shocktrade.server

import com.shocktrade.javascript.models.contest._
import com.shocktrade.server.data.{Claim, PortfolioData, WorkOrder}
import com.shocktrade.server.services.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.server.TradingQualificationEngine._
import com.shocktrade.server.data.PortfolioDAO._
import com.shocktrade.server.data.{Claim, PortfolioData, WorkOrder}
import com.shocktrade.server.services.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.YahooFinanceCSVQuotesService.YFCSVQuote
import org.scalajs.nodejs._
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.{Db, ObjectID}
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
  private val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)

  def run(): Unit = {
    val outcome = for {
      portfolioOpt <- portfolioDAO.flatMap(_.findNext(processingHost = os.hostname(), updateDelay = 5.seconds))
      claims <- processOrders(portfolioOpt)
    } yield claims

    outcome onComplete {
      case Success(claims) =>
        console.log(s"${claims.size} claim(s) were created")
        console.log("Process completed successfully")
      case Failure(e) =>
        console.error(s"Failed to process portfolio: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def processOrders(portfolioOpt: Option[PortfolioData]) = {
    portfolioOpt match {
      case Some(portfolio) =>
        console.log(s"Processing portfolio # ${portfolio._id}")

        // determine the as-of date
        val asOfTime = portfolio.lastUpdate.getOrElse(js.Date.now())
        console.log(s"as-of date: ${new js.Date(asOfTime)}")

        // attempt to find eligible orders
        val orders = portfolio.findEligibleOrders(asOfTime)

        // display the orders
        showOrders(portfolio, orders)

        // retrieve the quotes for the orders
        // and perform the qualification process
        for {
          quotes <- lookupOrderQuotes(portfolio, orders)
          workOrders = performQualification(portfolio, orders, quotes)
          outcome <- fulfillOrders(workOrders)
          _ <- removeEmptyPositions(portfolio._id.orNull)
        } yield outcome

      case None =>
        console.log("No eligible portfolio(s) found")
        Future.successful(Nil)
    }
  }

  private def fulfillOrders(workOrders: Seq[WorkOrder]) = {
    Future.sequence {
      workOrders map { workOrder =>
        val outcome = if (workOrder.order.isBuyOrder)
          portfolioDAO.flatMap(_.insertPosition(workOrder))
        else
          portfolioDAO.flatMap(_.reducePosition(workOrder))

        outcome foreach {
          case result if !result.result.isOk =>
            console.error(s"Order # ${workOrder.order._id} failed: result => ", result.result)
          case _ =>
        }
        outcome
      }
    }
  }

  private def lookupOrderQuotes(portfolio: PortfolioData, orders: Seq[Order]) = {
    // retrieve the quotes for each symbol
    val symbols = orders.flatMap(_.symbol.toList).distinct
    val quotes = csvQuoteSvc.getQuotes(cvsQuoteParams, symbols)
    quotes foreach (showQuotes(portfolio, _))
    quotes
  }

  private def performQualification(portfolio: PortfolioData, orders: Seq[Order], quotes: Seq[QuoteType]) = {
    val quoteMapping = js.Dictionary(quotes map (q => q.symbol -> q): _*)
    orders flatMap { order =>
      for {
        portfolioID <- portfolio._id.toOption
        symbol <- order.symbol.toOption
        quote <- quoteMapping.get(symbol)
        claim <- order.qualify(quote) match {
          case Success(claim) => Option(claim)
          case Failure(e) => console.warn(e.getMessage); None
        }
      } yield new WorkOrder(portfolioID, order, claim)
    }
  }

  private def removeEmptyPositions(portfolioID: ObjectID) = {
    portfolioDAO.flatMap(_.removeEmptyPositions(portfolioID)) map {
      case outcome if outcome.result.isOk =>
        console.log("zero positions: %d", outcome.result.nModified)
        outcome
      case outcome if outcome.result.isOk =>
        console.log("outcome => ", outcome)
        outcome
    }
  }

  private def showOrders(portfolio: PortfolioData, orders: Seq[Order]) = {
    console.log(s"Portfolio '${portfolio._id}' - ${orders.size} eligible order(s):")
    orders.zipWithIndex foreach { case (o, n) =>
      console.log(s"[${n + 1}] ${o.orderType} / ${o.symbol} @ ${o.price getOrElse "MARKET"} x ${o.quantity} - ${o.priceType} <${o._id}>")
    }
    console.log("")
  }

  private def showQuotes(portfolio: PortfolioData, quotes: Seq[QuoteType]) = {
    console.log(s"Portfolio '${portfolio._id}' - ${quotes.size} quote(s):")
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
    def qualify(quote: QuoteType) = Try {
      // ensure the quote's properties
      val exchange = quote.exchange orDie "Missing symbol"
      val tradeTime = quote.tradeDateTime.map(_.getTime()) orDie "Missing trade time"
      val volume = quote.volume orDie "Missing volume"
      val stockPrice = {
        if (order.isLimitOrder || order.isMarketOrder) quote.lastTrade orDie "Missing Market price"
        else if (order.isMarketAtCloseOrder) quote.close orDie "Missing Market close price"
        else die(s"Invalid price type (${order.priceType})")
      }

      // ensure the order's properties
      val orderTime = order.creationTime orDie "Missing order creation time"
      val quantity = order.quantity orDie "Missing order quantity"

      // If the volume is greater than the desired quantity
      // and the price is either Market or less than or equal to the limit price
      // and the transaction occurred AFTER the order was created
      if (orderTime > tradeTime) reject(s"out of time bounds", required = tradeTime, actual = orderTime)
      else if (volume < quantity) reject(s"insufficient volume", required = quantity, actual = volume)
      else if (order.isLimitOrder) {
        val limitPrice = order.price orDie "Missing LIMIT price"
        if (order.isBuyOrder && stockPrice > limitPrice) reject("Market price too high", required = limitPrice, actual = stockPrice)
        else if (order.isSellOrder && stockPrice < limitPrice) reject("Market price too low", required = limitPrice, actual = stockPrice)
      }

      // if all checks passed, return the claim
      new Claim(symbol = quote.symbol, exchange = exchange, price = stockPrice, quantity = quantity, asOfTime = tradeTime)
    }

    @inline
    def reject[S](message: String, required: js.Any, actual: js.Any): S = {
      die(s"Order # ${order._id}: $message (required: $required, actual: $actual)")
    }

  }

}