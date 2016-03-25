package com.shocktrade.processors

import java.util.Date

import akka.actor.Props
import akka.routing.RoundRobinPool
import com.shocktrade.processors.actors.CikNumberUpdateActor.UpdateMissingCikNumbers
import com.shocktrade.processors.actors.FinraRegShoUpdateActor.ProcessRegSHO
import com.shocktrade.processors.actors.NasdaqImportActor.NasdaqImport
import com.shocktrade.processors.actors.YahooCsvQuoteUpdateActor.RefreshAllQuotes
import com.shocktrade.processors.actors.YahooKeyStatisticsUpdateActor.RefreshAllKeyStatistics
import com.shocktrade.processors.actors.{CikNumberUpdateActor, NasdaqImportActor, YahooCsvQuoteUpdateActor, _}
import com.shocktrade.processors.actors.robots.TradingRobots
import play.api.Logger
import play.libs.Akka
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
  * ShockTrade Data Loading Processes
  * @author lawrence.daniels@gmail.com
  */
case class DataLoadingProcesses(reactiveMongoApi: ReactiveMongoApi) {
  implicit val ec = Akka.system.dispatcher

  // create the actors
  val cikNumberUpdateActor = Akka.system.actorOf(Props(new CikNumberUpdateActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "CikNumberUpdate")

  val equityShortInterestUpdateActor = Akka.system.actorOf(Props(new EquityShortInterestUpdateActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "EquityShortInterestUpdate")

  val finraRegShoUpdateActor = Akka.system.actorOf(Props(new FinraRegShoUpdateActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "FinraRegShoUpdate")

  val nasdaqImportActor = Akka.system.actorOf(Props(new NasdaqImportActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "NasdaqImport")

  val yahooCsvQuoteUpdateActor = Akka.system.actorOf(Props(new YahooCsvQuoteUpdateActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "YahooCsvQuoteUpdate")

  val yahooKeyStatisticsUpdateActor = Akka.system.actorOf(Props(new YahooKeyStatisticsUpdateActor(reactiveMongoApi))
    .withRouter(RoundRobinPool(nrOfInstances = 5)), name = "YahooKeyStatisticsUpdate")

  // start the trading robots
  val tradingRobots = TradingRobots(reactiveMongoApi)
  tradingRobots.start()

  // start the trading engine
  val tradingEngine = TradingEngine(reactiveMongoApi)
  tradingEngine.start()

  /**
    * Starts the process
    */
  def start() {
    Logger.info("Starting Financial Update Processes ...")

    // schedules CIK updates
    Akka.system.scheduler.schedule(1.hour, 3.days)(cikNumberUpdateActor ! UpdateMissingCikNumbers)

    // schedules NASDAQ updates
    Akka.system.scheduler.schedule(1.hour, 24.hours)(nasdaqImportActor ! NasdaqImport)

    // schedules key statistics updates
    Akka.system.scheduler.schedule(1.hour, 8.hours)(yahooKeyStatisticsUpdateActor ! RefreshAllKeyStatistics)

    // schedules Reg SHO updates
    Akka.system.scheduler.schedule(1.hour, 24.hours)(finraRegShoUpdateActor ! ProcessRegSHO(new Date()))

    // schedules stock quote updates
    Akka.system.scheduler.schedule(5.seconds, 30.minutes)(yahooCsvQuoteUpdateActor ! RefreshAllQuotes)

    ()
  }

}
