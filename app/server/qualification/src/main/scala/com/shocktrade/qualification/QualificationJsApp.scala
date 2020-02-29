package com.shocktrade.qualification

import com.shocktrade.qualification.routes.QualificationRoutes
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express.{Application, Express}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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

    // determine the web application port
    val port = process.port getOrElse "1337"

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

    // instantiate the qualification engine
    val cqm = new ContestQualificationModule()

    // define the API routes
    new QualificationRoutes(app, cqm)

    // start the listener
    app.listen(port, () => logger.log(s"Server now listening on port $port [${System.currentTimeMillis() - startTime} msec]"))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }
    ()
  }

}
