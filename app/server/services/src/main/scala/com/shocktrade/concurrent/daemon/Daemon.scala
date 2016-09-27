package com.shocktrade.concurrent.daemon

import com.shocktrade.services.LoggerFactory
import org.scalajs.nodejs._

import scala.concurrent.duration.FiniteDuration

/**
  * Represents a Daemon process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Daemon {

  def run(): Unit

}

/**
  * Daemon Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Daemon {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  /**
    * Schedules the collection of daemons for execution
    * @param daemons the given collection of [[DaemonRef daemon references]]
    */
  def schedule(daemons: Seq[DaemonRef]) = {
    daemons foreach { ref =>
      logger.info(s"Configuring '${ref.name}' to run every ${ref.frequency}, after an initial delay of ${ref.delay}...")
      setTimeout(() => {
        logger.info(s"Starting daemon '${ref.name}'...")
        ref.daemon.run()
        setInterval(() => ref.daemon.run(), ref.frequency)
      }, ref.delay)
    }
  }

  /**
    * Represents a reference to a daemon
    * @param name      the name of the daemon
    * @param daemon    the daemon instance
    * @param delay     the initial delay before the daemon runs on it's regular interval
    * @param frequency the interval with which the daemon shall run
    */
  case class DaemonRef(name: String, daemon: Daemon, delay: FiniteDuration, frequency: FiniteDuration)

}