package com.shocktrade.server.trading.robots

import akka.actor.Props
import com.shocktrade.server.trading.robots.TradingRobot.Invest
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.concurrent.Akka

import scala.concurrent.duration._

/**
 * Trading Robots
 * @author lawrence.daniels@gmail.com
 */
object TradingRobots {
  private lazy val logger = LoggerFactory.getLogger(getClass)
  private val system = Akka.system
  implicit val ec = system.dispatcher

  private val robots = Seq(("gadget", DayTradingStrategy) /*, ("daisy", DayTradingStrategy)*/) map { case (name, strategy) =>
    system.actorOf(Props(new TradingRobot(name, strategy)), name)
  }

  /**
   * Starts the trading robots
   */
  def start() {
    logger.info("Starting Trading Robots ...")
    system.scheduler.schedule(5.seconds, 15.minutes) {
      robots.foreach(_ ! Invest)
    }
    ()
  }

}
