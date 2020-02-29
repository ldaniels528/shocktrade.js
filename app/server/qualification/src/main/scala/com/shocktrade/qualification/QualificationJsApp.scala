package com.shocktrade.qualification

import com.shocktrade.qualification.routes.QualificationRoutes
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon._
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express.{Application, Express}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.{queue => Q}
import scala.scalajs.js.annotation.JSExport

/**
  * Qualification Server Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object QualificationJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]) {
    logger.log("Starting the Qualification Server...")

    // determine the port to listen on
    val startTime = System.currentTimeMillis()

    // determine the web application port, MongoDB and Zookeeper connection URLs
    val port = process.port getOrElse "1338"
    val dbConnectionString = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade"

    // create the trading clock instance
    implicit val tradingClock: TradingClock = new TradingClock()

    logger.info("Loading Express modules...")
    implicit val app: Application = Express()

    // setup the body parsers
    logger.log("Setting up body parsers...")
    app.use(BodyParser.json())
    app.use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // disable caching
    app.disable("etag")

    implicit val dbFuture: Future[Db] = Future.failed(new Exception())

    // instantiate the qualification engine
    val qualificationEngine = new OrderQualificationEngine(dbFuture)

    // define the API routes
    QualificationRoutes.init(app, qualificationEngine)

    // start the listener
    app.listen(port, () => logger.log("Server now listening on port %s [%d msec]", port, System.currentTimeMillis() - startTime))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }

    // schedule the daemons to run
    schedule(tradingClock, Seq(
      DaemonRef("ContestCloseOut", new ContestCloseOutEngine(dbFuture), kafkaReqd = false, delay = 0.seconds, frequency = 30.minutes),
      //DaemonRef("IntraDayQuote", new IntraDayQuoteDaemon(dbFuture), kafkaReqd = false, delay = 0.seconds, frequency = 30.minutes),
      DaemonRef("OrderQualification", qualificationEngine, kafkaReqd = false, delay = 0.seconds, frequency = 1.minutes))
    )
  }

}
