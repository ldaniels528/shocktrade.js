package com.shocktrade.daycycle

import com.shocktrade.concurrent.daemon.Daemon._
import com.shocktrade.daycycle.daemons._
import com.shocktrade.services.{LoggerFactory, TradingClock}
import org.scalajs.nodejs.Bootstrap
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.{queue => Q}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Day-Cycle Server Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object DayCycleJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) = {
    implicit val require = bootstrap.require

    val logger = LoggerFactory.getLogger(getClass)
    logger.info("Starting the Day-Cycle Server...")

    // determine the database connection URL
    val connectionString = process.env.get("db_connection") getOrElse "mongodb://localhost:27017/shocktrade"

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }

    logger.log("Loading MongoDB module...")
    implicit val mongo = MongoDB()

    // setup mongodb connection
    logger.log("Connecting to '%s'...", connectionString)
    implicit val dbFuture = mongo.MongoClient.connectFuture(connectionString)

    // create the trading clock instance
    val tradingClock = new TradingClock()

    //new KeyStatisticsUpdateDaemon(dbFuture).run()
    //new SecuritiesUpdateDaemon(dbFuture).execute(js.Date.now())

    // schedule the daemons to run
    schedule(
      tradingClock,
      DaemonRef("CikUpdate", new CikUpdateDaemon(dbFuture), delay = 4.hours, frequency = 12.hours),
      DaemonRef("CompanyListUpdate", new CompanyListUpdateDaemon(dbFuture), delay = 3.hours, frequency = 12.hours),
      DaemonRef("FullMarketUpdate", new FullMarketUpdateDaemon(dbFuture), delay = 1.hours, frequency = 12.hours),
      DaemonRef("KeyStatisticsUpdate", new KeyStatisticsUpdateDaemon(dbFuture), delay = 2.hours, frequency = 12.hours),
      DaemonRef("SecuritiesUpdate", new SecuritiesUpdateDaemon(dbFuture), delay = 0.seconds, frequency = 5.minutes)
    )
  }

}
