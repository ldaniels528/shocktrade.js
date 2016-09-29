package com.shocktrade.concurrent.daemon

import com.shocktrade.services.{LoggerFactory, TradingClock}
import org.scalajs.nodejs._

import scala.concurrent.duration.FiniteDuration

/**
  * Represents a Daemon process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Daemon {

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param tradingClock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  def isReady(tradingClock: TradingClock): Boolean

  /**
    * Executes the process
    */
  def run(tradingClock: TradingClock): Unit

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
  def schedule(tradingClock: TradingClock, daemons: DaemonRef*) = {
    daemons foreach { ref =>
      logger.info(s"Configuring '${ref.name}' to run every ${ref.frequency}, after an initial delay of ${ref.delay}...")
      setTimeout(() => {
        // attempt to run the daemon
        if (ref.daemon.isReady(tradingClock)) run(tradingClock, ref)

        // set the regular interval runs
        setInterval(() => if (ref.daemon.isReady(tradingClock)) run(tradingClock, ref), ref.frequency)
      }, ref.delay)
    }
  }

  private def run(tradingClock: TradingClock, ref: DaemonRef) = {
    if (ref.daemon.isReady(tradingClock)) {
      logger.info(s"Starting daemon '${ref.name}'...")
      ref.daemon.run(tradingClock)
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