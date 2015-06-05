package com.shocktrade.server.trading

import java.util.Date

import com.shocktrade.util.DateUtil

/**
 * Trading Clock
 * @author lawrence.daniels@gmail.com
 */
object TradingClock {

  /**
   * The time in milliseconds until the next trading day
   */
  def getDelayUntilTradingStartInMillis = DateUtil.getTradeStartTime.getTime - (new Date).getTime

  def isTradingActive = DateUtil.isTradingActive

  def isTradingActive(timeInMillis: Long) = DateUtil.isTradingActive(timeInMillis)

  def isTradingActive(date: Date) = DateUtil.isTradingActive(date)

}
