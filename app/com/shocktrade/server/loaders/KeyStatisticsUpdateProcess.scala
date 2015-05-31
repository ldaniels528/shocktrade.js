package com.shocktrade.server.loaders

import com.shocktrade.actors.YahooKeyStatisticsUpdateActor
import com.shocktrade.actors.YahooKeyStatisticsUpdateActor.RefreshAllKeyStatistics
import com.shocktrade.server.trading.TradingClock
import play.api.Logger
import play.libs.Akka

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * Yahoo! Key Statistics Update Actor
 * @author lawrence.daniels@gmail.com
 */
object KeyStatisticsUpdateProcess {
  private val system = Akka.system
  implicit val ec = system.dispatcher

  /**
   * Starts the process
   */
  def start() {
    Logger.info("Starting Key Statistics Update Process ...")
    system.scheduler.schedule(5.seconds, 30.minutes) {
      if (TradingClock.isTradingActive) {
        Logger.info("Loading symbols for Key Statistics update...")
        YahooKeyStatisticsUpdateActor ! RefreshAllKeyStatistics
      }
    }
    ()
  }

}
