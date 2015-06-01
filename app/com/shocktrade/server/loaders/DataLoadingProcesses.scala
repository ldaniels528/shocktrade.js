package com.shocktrade.server.loaders

import com.shocktrade.actors.{YahooKeyStatisticsUpdateActor, YahooCsvQuoteUpdateActor}
import com.shocktrade.actors.YahooCsvQuoteUpdateActor.RefreshAllQuotes
import com.shocktrade.actors.YahooKeyStatisticsUpdateActor.RefreshAllKeyStatistics
import com.shocktrade.server.trading.TradingClock
import play.api.Logger
import play.libs.Akka

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * ShockTrade Data Loading Process
 * @author lawrence.daniels@gmail.com
 */
object DataLoadingProcesses {
  private val system = Akka.system
  implicit val ec = system.dispatcher

  /**
   * Starts the process
   */
  def start() {
    Logger.info("Starting Financial Update Processes ...")

    // schedules stock quote updates
    system.scheduler.schedule(5.seconds, 30.minutes) {
      if (TradingClock.isTradingActive) {
        YahooCsvQuoteUpdateActor ! RefreshAllQuotes
      }
    }

    // schedules key statistics updates
    system.scheduler.schedule(1.hour, 8.hours)(YahooKeyStatisticsUpdateActor ! RefreshAllKeyStatistics)
    ()
  }

}
