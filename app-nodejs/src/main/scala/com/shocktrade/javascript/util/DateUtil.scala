package com.shocktrade.javascript.util

import scala.scalajs.js

/**
  * Date Util
  * @author lawrence.daniels@gmail.com
  */
object DateUtil {

  def getDelayUntilTradingStartInMillis: Double = -60000d

  def getTradeStartTime: js.Date = new js.Date(System.currentTimeMillis() - 60000d) // TODO

  def getTradeStopTime: js.Date = new js.Date(System.currentTimeMillis() + 60000d) // TODO

  /**
    * Trading in the U.S. occurs between 9am and 4pm Eastern Time.
    * This method returns true if the current time is between those hours.
    * @return true, if the current time is between 9:30am and 4pm Eastern Time.
    */
  def isTradingActive: Boolean = isTradingActive(System.currentTimeMillis())

  /**
    * Indicates whether the given date/time falls between 9am and 4pm Eastern Time.
    * @param date the given date/time
    * @return true, if the current time is between 9:30am and 4pm Eastern Time.
    */
  def isTradingActive(date: js.Date): Boolean = isTradingActive(date.getTime())

  /**
    * Trading in the U.S. occurs between 9am and 4pm Eastern Time.
    * This method returns true if the specified time is between those hours.
    * @param timeInMillis the given time in milliseconds
    * @return true, if the specified time is between 9:30am and 4pm Eastern Time.
    */
  def isTradingActive(timeInMillis: Double): Boolean = true // TODO

}
