package com.shocktrade.qualification

import com.shocktrade.common.dao.Claim
import com.shocktrade.common.dao.contest.PortfolioUpdateDAO._
import com.shocktrade.common.dao.contest.{OrderData, PortfolioData, WorkOrder}
import com.shocktrade.common.dao.securities.SecuritiesSnapshotDAO._
import com.shocktrade.common.models.contest._
import com.shocktrade.concurrent.Daemon
import com.shocktrade.qualification.OrderQualificationEngine._
import com.shocktrade.serverside.{LoggerFactory, TradingClock}
import org.scalajs.nodejs._
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.{Db, ObjectID}
import org.scalajs.nodejs.os.OS
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.DateHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Order Qualification Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class OrderQualificationEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  // load modules
  private implicit val os = OS()
  private implicit val moment = Moment()

  // get DAO and service references
  private val logger = LoggerFactory.getLogger(getClass)
  private val portfolioDAO = dbFuture.flatMap(_.getPortfolioUpdateDAO)
  private val snapshotDAO = dbFuture.flatMap(_.getSnapshotDAO)

  // internal fields
  private var lastRun: js.Date = new js.Date()
  private val separator = "=" * 80

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(clock: TradingClock) = true // clock.isTradingActive || clock.isTradingActive(lastRun)

  /**
    * Executes the process
    * @param clock the given [[TradingClock trading clock]]
    */
  override def run(clock: TradingClock): Unit = {
    val isMarketCloseEvent = !clock.isTradingActive && clock.isTradingActive(lastRun)
    val startTime = System.currentTimeMillis()
    val outcome = for {
      portfolios <- portfolioDAO.flatMap(_.find().toArrayFuture[PortfolioData])
      claims <- Future.sequence(portfolios.toSeq map (processOrders(_, isMarketCloseEvent))) map (_.flatten)
    } yield claims

    outcome onComplete {
      case Success(claims) =>
        lastRun = new js.Date(startTime)
        logger.log(separator)
        logger.log(s"${claims.size} claim(s) were created")
        logger.log("Process completed in %d msec", System.currentTimeMillis() - startTime)
      case Failure(e) =>
        logger.error(s"Failed to process portfolio: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def processOrders(portfolio: PortfolioData, isMarketCloseEvent: Boolean) = {
    logger.log(separator)
    logger.log(s"Processing portfolio # ${portfolio._id}")

    // determine the as-of date
    val asOfTime = portfolio.lastUpdate.flat.getOrElse(new js.Date())
    logger.log(s"as-of date: $asOfTime\n")

    // attempt to find eligible orders
    val orders = portfolio.findEligibleOrders(asOfTime)

    // display the orders
    showOrders(portfolio, orders)

    // retrieve the quotes for the orders
    // and perform the qualification process
    for {
      quotes <- lookupWorkQuotes(portfolio, orders)
      workOrders = performQualification(portfolio, orders, quotes)
      outcome <- fulfillOrders(workOrders)
      _ <- removeEmptyPositions(portfolio._id.orNull)
    } yield outcome
  }

  private def fulfillOrders(workOrders: Seq[WorkOrder]) = {
    logger.log(s"Processing order fulfillment...")
    Future.sequence {
      workOrders map { wo =>
        val outcome = if (wo.order.isBuyOrder) {
          //logger.log("Creating new position - %j", wo)
          portfolioDAO.flatMap(_.insertPosition(wo))
        } else {
          //logger.log("Reducing position - %j", wo)
          portfolioDAO.flatMap(_.reducePosition(wo))
        }

        outcome foreach { result =>
          logger.info(s"Order # ${wo.order._id}: ${wo.order.symbol} x ${wo.order.quantity} - result => ", result.result)
        }
        outcome
      }
    }
  }

  private def lookupWorkQuotes(portfolio: PortfolioData, orders: Seq[OrderLike]) = {
    val eligibleQuotes = Future.sequence(orders.map { order =>
      snapshotDAO.flatMap(_.findMatch(order) map {
        case Some(quote) => Some(order -> quote)
        case None => None
      }).map(_.toSeq)
    }).map(_.flatten)

    val workQuotes = eligibleQuotes map {
      _ map { case (order, quote) =>
        new WorkQuote(
          symbol = quote.symbol,
          exchange = order.exchange,
          lastTrade = quote.lastTrade,
          close = quote.lastTrade,
          tradeDateTime = quote.tradeDateTime.map(_.getTime()),
          volume = quote.volume
        )
      }
    }

    workQuotes foreach (showQuotes(portfolio, _))
    workQuotes
  }

  private def performQualification(portfolio: PortfolioData, orders: Seq[OrderData], quotes: Seq[WorkQuote]) = {
    logger.log(s"Performing qualification <portfolio ${portfolio._id.orNull}>")
    val quoteMapping = js.Dictionary(quotes flatMap (q => q.symbol.map(_ -> q).toOption): _*)
    orders flatMap { order =>
      for {
        portfolioID <- portfolio._id.toOption
        symbol <- order.symbol.toOption
        quote <- quoteMapping.get(symbol)
        orderWithPrice = if (order.isLimitOrder && order.price.nonEmpty) order else order.copy(price = quote.lastTrade)
        claim <- order.qualify(quote) match {
          case Success(claim) => Option(claim)
          case Failure(e) => logger.warn(e.getMessage); None
        }
      } yield new WorkOrder(portfolioID, orderWithPrice, claim)
    }
  }

  private def removeEmptyPositions(portfolioID: ObjectID) = {
    logger.log("removing zero-quantity positions...")
    portfolioDAO.flatMap(_.removeEmptyPositions(portfolioID)) map {
      case outcome if outcome.result.isOk =>
        logger.log("Zero-quantity positions: %d", outcome.result.nModified)
        outcome
      case outcome if outcome.result.isOk =>
        logger.log("outcome => ", outcome)
        outcome
    }
  }

  private def showOrders(portfolio: PortfolioData, orders: Seq[OrderData]) = {
    logger.log(s"Portfolio '${portfolio._id}' - ${orders.size} eligible order(s):")
    orders.zipWithIndex foreach { case (o, n) =>
      logger.log(s"[${n + 1}] ${o.orderType} / ${o.symbol} @ ${o.price getOrElse "MARKET"} x ${o.quantity} - ${o.priceType} <${o._id}>")
    }
    logger.log("")
  }

  private def showQuotes(portfolio: PortfolioData, quotes: Seq[WorkQuote]) = {
    logger.log(s"Portfolio '${portfolio._id}' - ${quotes.size} quote(s):")
    quotes.zipWithIndex foreach { case (q, n) =>
      logger.log(f"[${n + 1}] ${q.symbol} ${q.lastTrade} ${q.tradeDateTime} [${q.tradeDateTime.map(t => moment(new js.Date(t)).format("MM/DD/YYYY HH:mm:ss"))}]")
    }
    logger.log("")
  }

}

/**
  * Trading Qualification Engine Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object OrderQualificationEngine {

  @ScalaJSDefined
  class WorkQuote(val symbol: js.UndefOr[String],
                  val exchange: js.UndefOr[String],
                  val lastTrade: js.UndefOr[Double],
                  val close: js.UndefOr[Double],
                  val tradeDateTime: js.UndefOr[Double],
                  val volume: js.UndefOr[Double]) extends js.Object

  /**
    * Order Qualification Logic
    * @param order the given [[OrderLike order]]
    */
  final implicit class OrderQualification(val order: OrderLike) extends AnyVal {

    @inline
    def qualify(quote: WorkQuote) = Try {
      // ensure the quote's properties
      val symbol = quote.symbol orDie "Missing symbol"
      val exchange = quote.exchange orDie "Missing exchange"
      val tradeTime = quote.tradeDateTime.map(new js.Date(_)) orDie "Missing trade time"
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
      new Claim(symbol = symbol, exchange = exchange, price = stockPrice, quantity = quantity, asOfTime = tradeTime)
    }

    @inline
    def reject[S](message: String, required: js.Any, actual: js.Any): S = {
      die(s"Order # ${order._id}: $message (required: $required, actual: $actual)")
    }

  }

}