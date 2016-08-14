package com.shocktrade.server.tqm

import org.scalajs.nodejs._
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Shocktrade Trading Qualification Module (TQM) JavaScript Application
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object ShocktradeTQMJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) {
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

    // initialize the qualification module
    val qm = new TradingQualificationEngine(dbFuture)
    qm.run()

    setInterval(() => qm.run(), 30.seconds)
    ()
  }

}
