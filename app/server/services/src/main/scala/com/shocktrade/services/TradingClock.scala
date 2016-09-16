package com.shocktrade.services

import org.scalajs.nodejs.moment._
import org.scalajs.nodejs.moment.timezone._

import scala.scalajs.js

/**
  * Trading Clock
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TradingClock() {

  /**
    * The time in milliseconds until the next trading day
    */
  def getDelayUntilTradingStartInMillis(implicit moment: Moment, momentTz: MomentTimezone): Double = {
    getNextTradeStartTime.getTime - js.Date.now()
  }

  /**
    * The U.S. Stock Markets open at 9:30am Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  def getNextTradeStartTime(implicit moment: Moment, momentTz: MomentTimezone): js.Date = {
    val theMoment = moment().tz("America/New_York")
    val delta = theMoment.day() match {
      case SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY => 1
      case FRIDAY => 3
      case SATURDAY => 2
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.add(delta, "day").hour(9).minute(30).toDate()
  }

  @inline
  def getNextTradeStopTime(implicit moment: Moment, momentTz: MomentTimezone): js.Date = {
    moment(getNextTradeStartTime).hour(16).minute(0).toDate()
  }

  /**
    * The U.S. Stock Markets open at 9:30am Eastern Time
    * @return the stock market opening [[js.Date time]]
    */
  def getTradeStartTime(implicit moment: Moment, momentTz: MomentTimezone): js.Date = {
    val theMoment = moment().tz("America/New_York")
    val delta = theMoment.day() match {
      case MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY => 0
      case SATURDAY => 2
      case SUNDAY => 1
      case day => throw new IllegalArgumentException(s"Illegal day of week value ($day)")
    }
    theMoment.add(delta, "day").hour(9).minute(30).toDate()
  }

  @inline
  def getTradeStopTime(implicit moment: Moment, momentTz: MomentTimezone): js.Date = {
    moment(getTradeStartTime).hour(16).minute(0).toDate()
  }

  @inline
  def isTradingActive(implicit moment: Moment, momentTz: MomentTimezone): Boolean = {
    isTradingActive(new js.Date())
  }

  @inline
  def isTradingActive(timeInMillis: Double)(implicit moment: Moment, momentTz: MomentTimezone): Boolean = {
    isTradingActive(new js.Date(timeInMillis))
  }

  def isTradingActive(date: js.Date)(implicit moment: Moment, momentTz: MomentTimezone): Boolean = {
    val theMoment = moment(date).tz("America/New_York")
    val time = theMoment.format("HHmm").toInt
    val dayOfWeek = theMoment.day()
    dayOfWeek >= MONDAY && dayOfWeek <= FRIDAY && time >= 930 && time <= 1601
  }

}
