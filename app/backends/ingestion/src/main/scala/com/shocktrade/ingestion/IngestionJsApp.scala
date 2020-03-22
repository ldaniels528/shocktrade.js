package com.shocktrade.ingestion

import com.shocktrade.ingestion.daemons.cqm.ContestQualificationModule
import com.shocktrade.ingestion.daemons.eoddata.EodDataCompanyUpdateDaemon
import com.shocktrade.ingestion.daemons.nasdaq.NASDAQCompanyListUpdateDaemon
import com.shocktrade.ingestion.routes.QualificationRoutes
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import io.scalajs.nodejs.timers.Interval
import io.scalajs.nodejs.{process, setInterval, setTimeout}
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express.{Application, Express}
import io.scalajs.util.DurationHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
 * Ingestion Server Application
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object IngestionJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the Ingestion Server...")

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }

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

    // create the CQM instance
    val cqm = new ContestQualificationModule()

    // attach the routes
    new QualificationRoutes(app, cqm)

    // start the listener
    app.listen(port, () => logger.log(s"Server now listening on port $port [${System.currentTimeMillis() - startTime} msec]"))

    //setTimeout(() => new WikipediaCompanyLoader().run(), 1.second)

    // schedule the daemons
    //schedule(name = "Contest Qualification Module")(initialDelay = 0.minutes, frequency = 5.minutes)(() => cqm.execute(tradingClock.isTradingActive))
    //schedule(name = "[SEC.gov]CIK Update")(initialDelay = 1.minute, frequency = 3.days)(() => new CikUpdateDaemon().run(tradingClock))
    schedule(name = "EOD-Data Company Update")(initialDelay = 0.minutes, frequency = 2.days)(() => new EodDataCompanyUpdateDaemon().run(tradingClock))
    //schedule(name = "NASDAQ Company List Update")(initialDelay = 0.minutes, frequency = 3.days)(() => new NASDAQCompanyListUpdateDaemon().run(tradingClock))
  }

  def schedule[A](name: String)(initialDelay: FiniteDuration, frequency: FiniteDuration)(process: () => Future[A])(implicit tradingClock: TradingClock): Interval = {
    logger.info(s"Configuring daemon $name...")

    val onStart = () => logger.info(s"Launching $name...")
    val onSuccess = () => logger.info(s"Completed $name.")
    val onFail = (e: Throwable) => logger.info(s"Failed $name: ${e.getMessage}")

    def run(): Future[A] = {
      onStart()
      val outcome = process()
      outcome onComplete {
        case Success(_) => onSuccess()
        case Failure(e) => onFail(e)
      }
      outcome
    }

    setTimeout(() => run(), initialDelay)
    setInterval(() => run(), frequency)
  }

}
