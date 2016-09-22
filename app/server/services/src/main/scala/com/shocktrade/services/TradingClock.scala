package com.shocktrade.services

import com.shocktrade.services.TradingClock._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.moment._
import org.scalajs.nodejs.moment.timezone._
import org.scalajs.sjs.DateHelper._

import scala.scalajs.js

/**
  * Trading Clock
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TradingClock()(implicit require: NodeRequire) {
  // load modules
  private val moment = Moment()
  MomentTimezone()

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
  def getLastTradeStartTime = getLastTradeDay.hour(9).minute(30).toDate()

  /**
    * Returns the last trading start time. If Monday through Friday, it will return the current date at 4:00pm ET;
    * however, if the current day of week is Saturday or Sunday, it will return the previous Friday at 4:00pm ET.
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getLastTradeStopTime = getLastTradeDay.hour(16).minute(0).toDate()

  /**
    * Returns the last trading day. If Monday through Friday, it will return the current date;
    * however, if the current day of week is Saturday or Sunday, it will return the previous Friday.
    * @return the stock market opening [[js.Date time]]
    */
  private def getLastTradeDay = {
    val theMoment = moment().tz(NEW_YORK_TZ)
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
  def getNextTradeStartTime = getNextTradingDay.hour(9).minute(30).toDate()

  /**
    * The next trading end time for the U.S. Stock Markets (4:00pm Eastern Time)
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getNextTradeStopTime = getNextTradingDay.hour(16).minute(0).toDate()

  private def getNextTradingDay = {
    val theMoment = moment().tz(NEW_YORK_TZ)
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
  def getTradeStartTime = getTradingDay.hour(9).minute(30).toDate()

  /**
    * The U.S. Stock Markets open at 4:00pm Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  @inline
  def getTradeStopTime = getTradingDay.hour(16).minute(0).toDate()

  private def getTradingDay = {
    val delta = moment().tz(NEW_YORK_TZ).day() match {
      case MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY => 0
      case SATURDAY => 2
      case SUNDAY => 1
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    moment().tz(NEW_YORK_TZ).add(delta, "day")
  }

  @inline
  def isTradingActive: Boolean = isTradingActive(new js.Date())

  @inline
  def isTradingActive(timeInMillis: Double): Boolean = isTradingActive(new js.Date(timeInMillis))

  def isTradingActive(date: js.Date): Boolean = {
    val theMoment = moment(date).tz(NEW_YORK_TZ)
    val time = theMoment.format("HHmm").toInt
    val dayOfWeek = theMoment.day()
    dayOfWeek >= MONDAY && dayOfWeek <= FRIDAY && time >= 930 && time <= 1601
  }

  @inline
  def isWeekDay: Boolean = isWeekDay(new js.Date())

  def isWeekDay(date: js.Date): Boolean = {
    val theMoment = moment(date).tz(NEW_YORK_TZ)
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