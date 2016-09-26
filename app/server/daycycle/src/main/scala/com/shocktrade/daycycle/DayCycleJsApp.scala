package com.shocktrade.daycycle

import com.shocktrade.services.LoggerFactory
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB
import org.scalajs.nodejs.{Bootstrap, _}

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.{queue => Q}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Shocktrade Day-Cycle Server Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object DayCycleJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) = {
    implicit val require = bootstrap.require

    val logger = LoggerFactory.getLogger(getClass)
    logger.log("Starting the Day-Cycle Server...")

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

    // run the cik update process once every 24 hours
    val cikUpdateProcess = new CikUpdateProcess(dbFuture)
    setInterval(() => cikUpdateProcess.run(), 24.hours)
    cikUpdateProcess.run()

    /*
    // run the stock refresh process once every 30 minutes
    val csvQuoteRefresh = new SecuritiesUpdateProcess(dbFuture)
    setInterval(() => csvQuoteRefresh.run(), 5.minutes)
    csvQuoteRefresh.run()

    // run the key statistics update process once every 24 hours
    val keyStatisticsUpdateProcess = new KeyStatisticsUpdateProcess(dbFuture)
    setInterval(() => keyStatisticsUpdateProcess.run(), 24.hours)
    keyStatisticsUpdateProcess.run()*/
  }

}
