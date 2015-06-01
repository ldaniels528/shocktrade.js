package com.shocktrade.controllers

import akka.util.Timeout
import com.shocktrade.actors.YahooKeyStatisticsUpdateActor.RefreshAllKeyStatistics
import com.shocktrade.actors.{YahooKeyStatisticsUpdateActor, YahooCsvQuoteUpdateActor}
import com.shocktrade.actors.YahooCsvQuoteUpdateActor.RefreshAllQuotes
import com.shocktrade.server.trading.TradingClock
import com.shocktrade.util.DateUtil
import play.api.libs.json.Json.{obj => JS}
import play.api.mvc._
import play.libs.Akka

import scala.concurrent.duration._

/**
 * Trading Resources
 * @author lawrence.daniels@gmail.com
 */
object TradingController extends Controller {
  private val system = Akka.system
  implicit val ec = system.dispatcher

  def startStockQuoteUpdate = Action.async {
    implicit val timeout: Timeout = 120.seconds

    (YahooCsvQuoteUpdateActor ? RefreshAllQuotes).mapTo[Int] map { count =>
      Ok(JS("symbol_count" -> count))
    }
  }

  def startKeyStatisticsUpdate = Action.async {
    implicit val timeout: Timeout = 10.minutes

    (YahooKeyStatisticsUpdateActor ? RefreshAllKeyStatistics).mapTo[Int] map { count =>
      Ok(JS("symbol_count" -> count))
    }
  }

  /**
   * Returns a trading clock state object
   */
  def status(lastUpdateTimeMillis: Long) = Action {
    val active = DateUtil.isTradingActive(System.currentTimeMillis())
    val delay = DateUtil.getDelayUntilTradingStartInMillis
    val start = DateUtil.getTradeStartTime
    val end = DateUtil.getTradeStopTime
    var stateChanged = false

    // if the last update time was specified, add the state change indicator
    if (lastUpdateTimeMillis > 0) {
      stateChanged = active != TradingClock.isTradingActive(lastUpdateTimeMillis)
    }

    // capture the system time
    val sysTime = System.currentTimeMillis()

    Ok(JS(
      "stateChanged" -> stateChanged,
      "active" -> active,
      "sysTime" -> System.currentTimeMillis(),
      "delay" -> delay,
      "start" -> start,
      "end" -> end))
  }

  /**
   * Returns the delay (in milliseconds) until trading starts
   */
  def delayUntilTradingStart = Action {
    Ok(JS("delayInMillis" -> TradingClock.getDelayUntilTradingStartInMillis))
  }

}