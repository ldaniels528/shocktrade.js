package com.shocktrade.daycycle

import com.shocktrade.daycycle.daemons._
import com.shocktrade.daycycle.routes.DaemonRoutes
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon._
import io.scalajs.nodejs.process
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express.Express
import io.scalajs.npm.kafkanode
import io.scalajs.npm.kafkanode.Producer
import io.scalajs.npm.mongodb.{Db, MongoClient}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.existentials
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Day-Cycle Server Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object DayCycleJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the Day-Cycle Server...")

    // determine the port to listen on
    val startTime = System.currentTimeMillis()

    // determine the web application port, MongoDB and Zookeeper connection URLs
    val port = process.port getOrElse "1337"
    val dbConnectionString = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade"
    val zkConnectionString = process.zookeeperConnect getOrElse "localhost:2181"

    // create the trading clock instance
    implicit val tradingClock = new TradingClock()

    // setup mongodb connection
    logger.info("Connecting to '%s'...", dbConnectionString)
    implicit val dbFuture = MongoClient.connectFuture(dbConnectionString)

    // setup kafka connection
    logger.info("Connecting to '%s'...", zkConnectionString)
    implicit val kafkaClient = new kafkanode.Client(zkConnectionString)
    implicit val kafkaProducer = new kafkanode.Producer(kafkaClient)

    logger.info("Loading Express modules...")
    implicit val app = Express()

    // setup the body parsers
    logger.log("Setting up body parsers...")
    app.use(BodyParser.json())
    app.use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // disable caching
    app.disable("etag")

    // define the daemons
    val daemons = createDaemons(dbConnectionString)

    // setup all other routes
    DaemonRoutes.init(app, daemons)

    // start the listener
    app.listen(port, () => logger.log("Server now listening on port %s [%d msec]", port, System.currentTimeMillis() - startTime))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }

    // start the optional installation
    if (process.argv.contains("--install")) {
      logger.info("Running installer...")
      new Installer(dbConnectionString).install() onComplete {
        case Success(_) =>
          logger.info("Installation completed")
          launchDaemons(daemons)
        case Failure(e) =>
          logger.error(s"Failed during installation: ${e.getMessage}")
          e.printStackTrace()
          launchDaemons(daemons)
      }
    }
    else {
      launchDaemons(daemons)
    }
  }

  private def createDaemons(dbConnectionString: String)(implicit dbFuture: Future[Db], kafkaProducer: Producer) = {
    Seq(
      DaemonRef("BarChartProfileUpdate", new BarChartProfileUpdateDaemon(dbFuture), kafkaReqd = false, delay = 5.hours, frequency = 12.hours),
      DaemonRef("BloombergUpdate", new BloombergUpdateDaemon(dbFuture), kafkaReqd = false, delay = 5.hours, frequency = 12.hours),
      DaemonRef("CikUpdate", new CikUpdateDaemon(dbFuture), kafkaReqd = false, delay = 4.hours, frequency = 12.hours),
      DaemonRef("EodDataCompanyUpdate", new EodDataCompanyUpdateDaemon(dbFuture), kafkaReqd = false, delay = 1.hours, frequency = 12.hours),
      DaemonRef("KeyStatisticsUpdate", new KeyStatisticsUpdateDaemon(dbFuture), kafkaReqd = false, delay = 2.hours, frequency = 12.hours),
      DaemonRef("NADSAQCompanyUpdate", new NADSAQCompanyUpdateDaemon(dbFuture), kafkaReqd = false, delay = 3.hours, frequency = 12.hours),
      DaemonRef("SecuritiesUpdate", new SecuritiesUpdateDaemon(dbConnectionString), kafkaReqd = false, delay = 0.seconds, frequency = 1.minutes),

      // kafka-dependent daemons
      DaemonRef("SecuritiesRefreshKafka", new SecuritiesRefreshKafkaDaemon(dbFuture), kafkaReqd = true, delay = 10.days, frequency = 3.days)
    )
  }

  private def launchDaemons[T](daemons: Seq[DaemonRef[T]])(implicit tradingClock: TradingClock, kafkaProducer: Producer) {
    // separate the daemons by Kafka dependency
    val (daemonsKafka, daemonsNonKafka) = daemons.partition(_.kafkaReqd)

    // start the daemons without a Kafka dependency
    schedule(tradingClock, daemonsNonKafka)

    // wait for the Kafka producer to be ready, then schedule the Kafka-dependent daemons to run
    kafkaProducer.onReady(() => schedule(tradingClock, daemonsKafka))
  }

}
