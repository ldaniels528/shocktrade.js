package com.shocktrade.server.trading

import java.util.Date

import akka.util.Timeout
import com.shocktrade.actors.WebSockets
import com.shocktrade.actors.WebSockets.ContestUpdated
import com.shocktrade.models.contest.Contest
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.libs.Akka
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * Trading Engine
  * @author lawrence.daniels@gmail.com
  */
case class TradingEngine(reactiveMongoApi: ReactiveMongoApi) {
  private val system = Akka.system
  private implicit val timeout: Timeout = 5.seconds
  private var lastEffectiveDate: DateTime = computeInitialFulfillmentDate
  private val frequency = 1.minute // TODO should be 5 (in minutes)
  private val contestDAO = ContestDAO(reactiveMongoApi)
  private val orderProcessor = OrderProcessor(reactiveMongoApi)

  /**
    * Starts the Order Processing System
    */
  def start()(implicit ec: ExecutionContext) {
    Logger.info("Starting Order Processing System ...")

    // process margin account interest once per hour
    system.scheduler.schedule(1.minute, 1.hours)(applyMarginInterest())

    // process orders once every 5 minutes
    system.scheduler.schedule(5.seconds, frequency)(processOrders())
    ()
  }

  /**
    * Apply margin account interest
    */
  def applyMarginInterest()(implicit ec: ExecutionContext, timeout: Timeout) = {
    try {
      Logger.info(s"Applying interest charges for margin accounts...")
      contestDAO.findActiveContests.enumerate().apply(Iteratee.foreach { contest =>
        contestDAO.applyMarginInterest(contest)
      })

    } catch {
      case e: Exception =>
        Logger.error("Error processing margin account interest", e)
    }
  }

  private def processOrders()(implicit ec: ExecutionContext) {
    val currentTime = new Date()
    try {
      Logger.info(s"Processing order fulfillment [as of $lastEffectiveDate]...")
      contestDAO.getActiveContests(lastEffectiveDate, lastEffectiveDate.minusMinutes(frequency.toMinutes.toInt)).enumerate().apply(Iteratee.foreach { contest =>
        processOrders(contest, lastEffectiveDate)
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  def processOrders(contest: Contest, asOfDate: DateTime)(implicit ec: ExecutionContext) {
    val startTime = System.currentTimeMillis()

    // if trading was active during the as-of date
    Try(Await.result(orderProcessor.processContest(contest, asOfDate), 5.seconds)) match {
      case Success(updateCount) =>
        val elapsedTime = System.currentTimeMillis() - startTime
        Logger.info(s"Finished processing orders in $elapsedTime msec(s)")

        // update the processing info
        contestDAO.updateProcessingStats(contest.id, executionTime = elapsedTime, asOfDate = asOfDate)

        // if an update occurred, notify the users
        if (updateCount > 0) {
          contestDAO.findContestByID(contest.id, fields = Nil) foreach {
            _ foreach (updatedContest => WebSockets ! ContestUpdated(updatedContest))
          }
        }

      case Failure(e) =>
        Logger.error(s"An error occur while processing contest '${contest.name}'", e)
    }
  }

  private def computeInitialFulfillmentDate: Date = {
    val tradeStart = getTradeStartTime
    val currentTime = new Date()
    if (tradeStart >= currentTime) currentTime else tradeStart
  }

}
