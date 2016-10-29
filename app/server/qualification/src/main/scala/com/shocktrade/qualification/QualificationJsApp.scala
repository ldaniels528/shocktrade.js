package com.shocktrade.qualification

import com.shocktrade.qualification.routes.QualificationRoutes
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon._
import org.scalajs.nodejs._
import org.scalajs.nodejs.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import org.scalajs.nodejs.express.Express
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
  private val logger = LoggerFactory.getLogger(getClass)

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) = {
    implicit val require = bootstrap.require

    logger.log("Starting the Qualification Server...")

    // determine the port to listen on
    val startTime = System.currentTimeMillis()

    // determine the web application port, MongoDB and Zookeeper connection URLs
    val port = process.port getOrElse "1338"
    val dbConnectionString = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade"

    // create the trading clock instance
    implicit val tradingClock = new TradingClock()

    logger.info("Loading Express modules...")
    implicit val express = Express()
    implicit val app = express()

    // setup the body parsers
    logger.log("Setting up body parsers...")
    val bodyParser = BodyParser()
    app.use(bodyParser.json())
    app.use(bodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // setup mongodb connection
    logger.log("Loading MongoDB module...")
    implicit val mongo = MongoDB()
    logger.log("Connecting to '%s'...", dbConnectionString)
    implicit val dbFuture = mongo.MongoClient.connectFuture(dbConnectionString)

    // disable caching
    app.disable("etag")

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
      //DaemonRef("IntraDayQuote", new IntraDayQuoteDaemon(dbFuture), delay = 0.seconds, frequency = 30.minutes),
      DaemonRef("OrderQualification", qualificationEngine, kafkaReqd = false, delay = 0.seconds, frequency = 1.minutes))
    )
  }

}
