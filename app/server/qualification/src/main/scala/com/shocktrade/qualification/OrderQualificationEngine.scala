package com.shocktrade.qualification

import com.shocktrade.common.models.contest._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon
import io.scalajs.npm.mongodb.{Db, UpdateWriteOpResultObject}
import io.scalajs.util.DateHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.util.{Failure, Success, Try}

/**
  * Order Qualification Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class OrderQualificationEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[Seq[UpdateWriteOpResultObject]] {
  // get DAO and service references
  private val logger = LoggerFactory.getLogger(getClass)

  // internal fields
  private var lastRun: js.Date = new js.Date()
  private val separator = "=" * 90

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
  override def run(clock: TradingClock): Future[Seq[UpdateWriteOpResultObject]] = {
    val isMarketCloseEvent = !clock.isTradingActive && clock.isTradingActive(lastRun)
    val outcome = qualifyAll(isMarketCloseEvent)
    outcome onComplete {
      case Success((claims, startTime, processedTime)) =>
        lastRun = startTime
        logger.log(separator)
        logger.log(s"${claims.size} claim(s) were created")
        logger.log("Process completed in %d msec", processedTime)
      case Failure(e) =>
        logger.error(s"Failed to process portfolio: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome.map(_._1)
  }

  def qualifyAll(isMarketCloseEvent: Boolean): Future[(Seq[UpdateWriteOpResultObject], Date, Long)] = ???

}

/**
  * Trading Qualification Engine Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object OrderQualificationEngine {

  trait Claim extends js.Object

  trait PricingQuote extends js.Object {
    def lastTrade: js.UndefOr[Double]
  }

  trait UserInfo extends js.Object {
    def wallet: Double
  }

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
  final implicit class OrderQualificationLogic(val order: OrderLike) extends AnyVal {

    @inline
    def qualify(quote: WorkQuote): Try[Claim] = Try {
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
     ??? // new Claim(symbol = symbol, exchange = exchange, price = stockPrice, quantity = quantity, asOfTime = tradeTime)
    }

    @inline
    def reject[S](message: String, required: js.Any, actual: js.Any): S = {
      die(s"Order # ${order.orderID}: $message (required: $required, actual: $actual)")
    }

  }

}