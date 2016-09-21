package com.shocktrade.services

import org.scalajs.sjs.DateHelper._
import com.shocktrade.services.TradingClock._
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.nodejs.moment._
import org.scalajs.nodejs.moment.timezone._

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
  def getDelayUntilTradingStartInMillis: Double = {
    getNextTradeStartTime - new js.Date()
  }

  /**
    * The U.S. Stock Markets open at 9:30am Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  def getNextTradeStartTime: js.Date = {
    val theMoment = moment().tz(NEW_YORK_TZ)
    val delta = theMoment.day() match {
      case SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY => 1
      case FRIDAY => 3
      case SATURDAY => 2
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.add(delta, "day").hour(9).minute(30).toDate()
  }

  @inline
  def getNextTradeStopTime: js.Date = {
    moment(getNextTradeStartTime).hour(16).minute(0).toDate()
  }

  /**
    * The U.S. Stock Markets open at 9:30am Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  def getTradeStartTime: js.Date = {
    val theMoment = moment().tz(NEW_YORK_TZ)
    val delta = theMoment.day() match {
      case MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY => 0
      case SATURDAY => 2
      case SUNDAY => 1
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.add(delta, "day").hour(9).minute(30).toDate()
  }

  @inline
  def getTradeStopTime: js.Date = {
    moment(getTradeStartTime).hour(16).minute(0).toDate()
  }

  @inline
  def isTradingActive: Boolean = {
    isTradingActive(new js.Date())
  }

  @inline
  def isTradingActive(timeInMillis: Double): Boolean = {
    isTradingActive(new js.Date(timeInMillis))
  }

  def isTradingActive(date: js.Date): Boolean = {
    val theMoment = moment(date).tz(NEW_YORK_TZ)
    val time = theMoment.format("HHmm").toInt
    val dayOfWeek = theMoment.day()
    dayOfWeek >= MONDAY && dayOfWeek <= FRIDAY && time >= 930 && time <= 1601
  }

}

/**
  * Trading Clock Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TradingClock {
  private val NEW_YORK_TZ = "America/New_York"

}