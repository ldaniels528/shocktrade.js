package com.shocktrade.server

import com.shocktrade.server.services.NASDAQIntraDayQuotesService
import org.scalajs.nodejs._
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.{queue => Q}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Shocktrade Server JavaScript Application
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object ShocktradeServerJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) = {
    implicit val require = bootstrap.require

    // determine the port to listen on
    val connectionString = process.env.get("db_connection") getOrElse "mongodb://localhost:27017/shocktrade"

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      console.error("An uncaught exception was fired:")
      console.error(err.stack)
    }

    console.log("Loading MongoDB module...")
    implicit val mongo = MongoDB()

    // setup mongodb connection
    console.log("Connecting to '%s'...", connectionString)
    val dbFuture = mongo.MongoClient.connectFuture(connectionString)

    val svc = new NASDAQIntraDayQuotesService()
    svc.getQuotes("AAPL") foreach { quote =>
      console.log("quote => %j", quote)
    }

    // run the qualification engine once every 30 minutes
    val qm = new TradingQualificationEngine(dbFuture)
    setInterval(() => qm.run(), 30.minutes)
    //qm.run() // TODO for testing only

    // run the stock refresh loader once every 30 minutes
    val stockLoader = new StockRefreshLoader(dbFuture)
    setInterval(() => stockLoader.run(), 4.hours)
    //stockLoader.run() // TODO for testing only
  }

}
