package com.shocktrade.server.robots

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.ldaniels528.tabular.Tabular
import com.shocktrade.models.contest.Contests
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.robots.TradingRobot.Invest

import scala.concurrent.duration._

/**
 * Represents an autonomous Trading Robot
 * @author lawrence.daniels@gmail.com
 */
case class TradingRobot(name: String, strategy: TradingStrategy) extends Actor with ActorLogging {
  implicit val timeout: Timeout = 10.seconds
  private val tabular = new Tabular()

  import context.dispatcher

  override def receive = {
    case Invest => invest()

    case message =>
      log.info(s"Unhandled message: $message (${message.getClass.getName})")
      unhandled(message)
  }

  private def invest() {
    for {
    // lookup the quotes using our trading strategy
      quotes <- chooseStocks(strategy)

      // first let's retrieve the contests I've involved in ...
      contests <- Contests.findContestsByPlayerName(name)()

    } {
      log.info(s"$name: I'm playing ${contests.size} games right now....")
      (1 to contests.size) zip contests foreach { case (n, c) => log.info(f"$name: [$n%02d] ${c.name}") }
      log.info("")

      log.info(s"$name: Identified ${quotes.size} quote(s) for potential investment...")
      tabular.transform(quotes) foreach log.info
      quotes foreach { quote =>

      }
    }
  }

  private def chooseStocks(strategy: TradingStrategy) = {
    StockQuotes.findQuotes(strategy.getFilter)
  }

}

/**
 * Trading Robot Singleton
 * @author lawrence.daniels@gmail.com
 */
object TradingRobot {

  case object Invest

}
