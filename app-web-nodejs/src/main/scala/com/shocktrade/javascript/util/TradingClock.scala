package com.shocktrade.javascript.util

import scala.scalajs.js

/**
  * Trading Clock
  * @author lawrence.daniels@gmail.com
  */
object TradingClock {

  /**
    * The time in milliseconds until the next trading day
    */
  def getDelayUntilTradingStartInMillis = DateUtil.getTradeStartTime.getTime - js.Date.now()

  def isTradingActive = DateUtil.isTradingActive

  def isTradingActive(timeInMillis: Double) = DateUtil.isTradingActive(timeInMillis)

  def isTradingActive(date: js.Date) = DateUtil.isTradingActive(date)

}
