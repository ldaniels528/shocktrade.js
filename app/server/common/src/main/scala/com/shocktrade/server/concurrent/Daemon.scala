package com.shocktrade.server.concurrent

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import io.scalajs.nodejs._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  * Represents a Daemon process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Daemon[+T] {

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  def isReady(clock: TradingClock): Boolean

  /**
    * Executes the process
    */
  def run(clock: TradingClock): Future[T]

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
    * @param ref   the given [[DaemonRef daemon reference]]
    */
  def run[T](clock: TradingClock, ref: DaemonRef[T]) = {
    if (ref.daemon.isReady(clock)) start(clock, ref)
  }

  /**
    * Executes the daemon
    * @param clock the given [[TradingClock trading clock]]
    * @param ref   the given [[DaemonRef daemon reference]]
    */
  def start[T](clock: TradingClock, ref: DaemonRef[T]) = {
    logger.info(s"Starting daemon '${ref.name}'...")
    ref.daemon.run(clock)
  }


  /**
    * Schedules the collection of daemons for execution
    * @param daemons the given collection of [[DaemonRef daemon references]]
    */
  def schedule[T](clock: TradingClock, daemons: Seq[DaemonRef[T]]) = {
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
  case class DaemonRef[+T](name: String, daemon: Daemon[T], kafkaReqd: Boolean, delay: FiniteDuration, frequency: FiniteDuration)

}