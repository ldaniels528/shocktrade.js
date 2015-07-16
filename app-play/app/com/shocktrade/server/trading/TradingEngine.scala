package com.shocktrade.server.trading

import java.util.Date

import akka.util.Timeout
import com.shocktrade.actors.ContestActor.{ProcessOrders, _}
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.libs.Akka

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * Trading Engine
 * @author lawrence.daniels@gmail.com
 */
object TradingEngine {
  private val system = Akka.system
  private implicit val timeout: Timeout = 5.seconds
  private var lastEffectiveDate: DateTime = computeInitialFulfillmentDate
  private val frequency = 1.minute // TODO should be 5 (in minutes)

  /**
   * Starts the Order Processing System
   */
  def start() {
    Logger.info("Starting Order Processing System ...")

    // process orders once every 5 minutes
    system.scheduler.schedule(5.seconds, frequency)(processOrders())

    // process orders once per hour
    system.scheduler.schedule(1.minute, 4.hours)(applyMarginInterest())
    ()
  }

  def applyMarginInterest()(implicit ec: ExecutionContext, timeout: Timeout) = {
    try {
      Logger.info(s"Applying interest charges for margin accounts...")
      ContestDAO.findActiveContests.enumerate().apply(Iteratee.foreach { contest =>
        Contests ! ApplyMarginInterest(contest)
      })

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  private def processOrders() {
    val currentTime = new Date()
    try {
      Logger.info(s"Processing order fulfillment [as of $lastEffectiveDate]...")
      ContestDAO.getActiveContests(lastEffectiveDate, lastEffectiveDate.minusMinutes(frequency.toMinutes.toInt)).enumerate().apply(Iteratee.foreach { contest =>
        Contests ! ProcessOrders(contest, lastEffectiveDate)
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  private def computeInitialFulfillmentDate: Date = {
    val tradeStart = getTradeStartTime
    val currentTime = new Date()
    if (tradeStart >= currentTime) currentTime else tradeStart
  }

}
