package com.shocktrade.server.common

import com.shocktrade.server.common.TradingClock._
import io.scalajs.npm.moment._
import io.scalajs.npm.moment.timezone._
import io.scalajs.util.DateHelper._

import scala.scalajs.js

/**
  * Trading Clock
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TradingClock() {
  // make sure the modules are loaded
  Moment
  MomentTimezone

  /**
    * The time in milliseconds until the next trading day
    */
  @inline
  def getDelayUntilTradingStartInMillis: Double = getNextTradeStartTime - new js.Date()

  /**
    * Returns the last trading start time. If Monday through Friday, it will return the current date at 9:30am ET;
    * however, if the current day of week is Saturday or Sunday, it will return the previous Friday at 9:30am ET.
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getLastTradeStartTime: js.Date = getLastTradeDay.hour(9).minute(30).toDate()

  /**
    * Returns the last trading start time. If Monday through Friday, it will return the current date at 4:00pm ET;
    * however, if the current day of week is Saturday or Sunday, it will return the previous Friday at 4:00pm ET.
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getLastTradeStopTime: js.Date = getLastTradeDay.hour(16).minute(0).toDate()

  /**
    * Returns the last trading day. If Monday through Friday, it will return the current date;
    * however, if the current day of week is Saturday or Sunday, it will return the previous Friday.
    * @return the stock market opening [[js.Date time]]
    */
  private def getLastTradeDay = {
    val theMoment = Moment().tz(NEW_YORK_TZ)
    val delta = theMoment.day() match {
      case MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY => 0
      case SATURDAY => 1
      case SUNDAY => 2
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.subtract(delta, "day")
  }

  /**
    * The next trading end time for the U.S. Stock Markets (9:30am Eastern Time)
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getNextTradeStartTime: js.Date = getNextTradingDay.hour(9).minute(30).toDate()

  /**
    * The next trading end time for the U.S. Stock Markets (4:00pm Eastern Time)
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getNextTradeStopTime: js.Date = getNextTradingDay.hour(16).minute(0).toDate()

  private def getNextTradingDay = {
    val theMoment = Moment().tz(NEW_YORK_TZ)
    val delta = theMoment.day() match {
      case SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY => 1
      case FRIDAY => 3
      case SATURDAY => 2
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.add(delta, "day")
  }

  /**
    * The U.S. Stock Markets open at 9:30am Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getTradeStartTime : js.Date= getTradingDay.hour(9).minute(30).toDate()

  /**
    * The U.S. Stock Markets open at 4:00pm Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getTradeStopTime : js.Date= getTradingDay.hour(16).minute(0).toDate()

  private def getTradingDay = {
    val delta = Moment().tz(NEW_YORK_TZ).day() match {
      case MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY => 0
      case SATURDAY => 2
      case SUNDAY => 1
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    Moment().tz(NEW_YORK_TZ).add(delta, "day")
  }

  @inline
  def isTradingActive: Boolean = isTradingActive(new js.Date())

  @inline
  def isTradingActive(timeInMillis: Double): Boolean = isTradingActive(new js.Date(timeInMillis))

  @inline
  def isTradingActive(date: js.Date): Boolean = {
    val theMoment = Moment(date).tz(NEW_YORK_TZ)
    val time = theMoment.format("HHmm").toInt
    val dayOfWeek = theMoment.day()
    dayOfWeek >= MONDAY && dayOfWeek <= FRIDAY && time >= 930 && time <= 1601
  }

  @inline
  def isWeekDay: Boolean = isWeekDay(new js.Date())

  @inline
  def isWeekDay(date: js.Date): Boolean = {
    val theMoment = Moment(date).tz(NEW_YORK_TZ)
    val dayOfWeek = theMoment.day()
    dayOfWeek >= MONDAY && dayOfWeek <= FRIDAY
  }

}

/**
  * Trading Clock Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TradingClock {
  private val NEW_YORK_TZ = "America/New_York"

}