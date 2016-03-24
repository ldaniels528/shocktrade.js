package com.shocktrade.controllers

import javax.inject.Inject

import com.shocktrade.server.trading.TradingClock
import com.shocktrade.util.DateUtil
import play.api.libs.json.Json.{obj => JS}
import play.api.mvc._
import play.libs.Akka
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

/**
  * Trading Resources
  * @author lawrence.daniels@gmail.com
  */
class TradingController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents {
  implicit val ec = Akka.system.dispatcher

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