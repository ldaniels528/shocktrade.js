package com.shocktrade.server.loaders

import java.util.Date

import com.shocktrade.server.actors.CikNumberUpdateActor.UpdateMissingCikNumbers
import com.shocktrade.server.actors.FinraRegShoUpdateActor.ProcessRegSHO
import com.shocktrade.server.actors.NasdaqImportActor.NasdaqImport
import com.shocktrade.server.actors.YahooCsvQuoteUpdateActor.RefreshAllQuotes
import com.shocktrade.server.actors.YahooKeyStatisticsUpdateActor.RefreshAllKeyStatistics
import com.shocktrade.server.actors._
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

    // schedules CIK updates
    system.scheduler.schedule(1.hour, 3.days)(CikNumberUpdateActor ! UpdateMissingCikNumbers)

    // schedules NASDAQ updates
    system.scheduler.schedule(1.hour, 24.hours)(NasdaqImportActor ! NasdaqImport)

    // schedules key statistics updates
    system.scheduler.schedule(1.hour, 8.hours)(YahooKeyStatisticsUpdateActor ! RefreshAllKeyStatistics)

    // schedules Reg SHO updates
    system.scheduler.schedule(1.hour, 24.hours)(FinraRegShoUpdateActor ! ProcessRegSHO(new Date()))

    // schedules stock quote updates
    system.scheduler.schedule(5.seconds, 30.minutes)(YahooCsvQuoteUpdateActor ! RefreshAllQuotes)

    ()
  }

}
