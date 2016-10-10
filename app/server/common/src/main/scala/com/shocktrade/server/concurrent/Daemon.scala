package com.shocktrade.server.concurrent

import java.util.UUID

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import org.scalajs.nodejs._

import scala.concurrent.duration.FiniteDuration

/**
  * Represents a Daemon process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Daemon {

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  def isReady(clock: TradingClock): Boolean

  /**
    * Executes the process
    */
  def run(clock: TradingClock): Unit

}

/**
  * Daemon Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Daemon {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  /**
    * Executes the daemon if it's eligible
    * @param clock the given [[TradingClock trading clock]]
    * @param ref the given [[DaemonRef daemon reference]]
    */
  def run(clock: TradingClock, ref: DaemonRef) = {
    if (ref.daemon.isReady(clock)) start(clock, ref)
  }

  /**
    * Executes the daemon
    * @param clock the given [[TradingClock trading clock]]
    * @param ref the given [[DaemonRef daemon reference]]
    */
  def start(clock: TradingClock, ref: DaemonRef) = {
    logger.info(s"Starting daemon '${ref.name}'...")
    ref.daemon.run(clock)
  }


  /**
    * Schedules the collection of daemons for execution
    * @param daemons the given collection of [[DaemonRef daemon references]]
    */
  def schedule(clock: TradingClock, daemons: Seq[DaemonRef]) = {
    daemons foreach { ref =>
      logger.info(s"Configuring '${ref.name}' to run every ${ref.frequency}, after an initial delay of ${ref.delay}...")
      setTimeout(() => {
        // attempt to run the daemon
        run(clock, ref)

        // set the regular interval runs of the daemon
        setInterval(() => run(clock, ref), ref.frequency)
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
  case class DaemonRef(name: String, daemon: Daemon, delay: FiniteDuration, frequency: FiniteDuration) {
    val id = UUID.randomUUID().toString
  }

}