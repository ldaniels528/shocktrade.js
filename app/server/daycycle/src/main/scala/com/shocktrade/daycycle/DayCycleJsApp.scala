package com.shocktrade.daycycle

import com.shocktrade.daycycle.daemons._
import com.shocktrade.services.LoggerFactory
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.MongoDB
import org.scalajs.nodejs.{Bootstrap, duration2Int, _}

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

    // define the daemons
    val daemons = js.Array(
      DaemonRef("CompanyListUpdate", new CompanyListUpdateDaemon(dbFuture), delay = 1.hour, frequency = 24.hours),
      DaemonRef("CikUpdate", new CikUpdateDaemon(dbFuture), delay = 4.hours, frequency = 24.hours),
      DaemonRef("SecuritiesUpdate", new SecuritiesUpdateDaemon(dbFuture), delay = 15.seconds, frequency = 5.minutes),
      DaemonRef("KeyStatisticsUpdate", new KeyStatisticsUpdateDaemon(dbFuture), delay = 2.hours, frequency = 24.hours)
    )

    // schedule the daemons to run
    daemons foreach { ref =>
      logger.info(s"Configuring '${ref.name}' to run every ${ref.frequency}, after an initial delay of ${ref.delay}...")
      setInterval(() => setTimeout(() => ref.daemon.run(), ref.delay), ref.frequency)
    }
  }

  /**
    * Represents a reference to a daemon
    * @param name the name of the daemon
    * @param daemon the daemon instance
    * @param delay the initial delay before the daemon runs on it's regular interval
    * @param frequency the interval with which the daemon shall run
    */
  case class DaemonRef(name: String, daemon: Daemon, delay: FiniteDuration, frequency: FiniteDuration)

}
