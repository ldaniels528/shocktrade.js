package com.shocktrade.qualification

import com.shocktrade.server.concurrent.Daemon._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.common.ProcessHelper._
import org.scalajs.nodejs._
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.{queue => Q}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Qualification Server Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object QualificationJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) = {
    implicit val require = bootstrap.require

    val logger = LoggerFactory.getLogger(getClass)
    logger.log("Starting the Qualification Server...")

    // determine the database connection URL
    val connectionString = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade"

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

    // schedule the daemons to run
    schedule(tradingClock, Seq(
      //DaemonRef("IntraDayQuote", new IntraDayQuoteDaemon(dbFuture), delay = 0.seconds, frequency = 30.minutes),
      DaemonRef("OrderQualification", new OrderQualificationEngine(dbFuture), delay = 0.seconds, frequency = 1.minutes))
    )
  }

}
