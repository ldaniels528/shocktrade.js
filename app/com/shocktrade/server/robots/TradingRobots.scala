package com.shocktrade.server.robots

import akka.actor.Props
import com.shocktrade.server.robots.TradingRobot.Invest
import play.api.Play.current
import play.api.libs.concurrent.Akka

import scala.concurrent.duration._

/**
 * TradingRobots
 */
object TradingRobots {
  val system = Akka.system
  val robots = Seq(("gadget", DayTradingStrategy), ("daisy", DayTradingStrategy)) map { case (name, strategy) =>
    system.actorOf(Props(new TradingRobot(name, strategy)), name)
  }

  import system.dispatcher

  /**
   * Starts the trading robots
   */
  def start(): Unit = {
    system.scheduler.schedule(5.seconds, 15.minutes, new Runnable {
      override def run(): Unit = robots.foreach(_ ! Invest)
    })
  }

}
