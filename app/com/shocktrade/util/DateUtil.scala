package com.shocktrade.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, TimeZone}

import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

/**
 * ShockTrade Date Utility
 * @author lawrence.daniels@gmail.com
 */
object DateUtil {

  /**
   * Indicates whether days, minutes, seconds, and/or milliseconds are set.
   */
  def hasTime(date: Date): Boolean = {
    import Calendar._

    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.get(HOUR_OF_DAY) != 0 || cal.get(MINUTE) != 0 || cal.get(SECOND) != 0 || cal.get(MILLISECOND) != 0
  }

  /**
   * Clears the time portions of the given calendar
   * @param cal the given { @link Calendar calendar}
   */
  def clearTime(cal: Calendar): Date = {
    import Calendar._

    cal.set(HOUR_OF_DAY, 0)
    cal.set(MINUTE, 0)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime
  }

  /**
   * Clears the days, minutes, seconds, and/or milliseconds from the date, resulting
   * in midnight in the current time zone.
   */
  def clearTime(date: Date): Date = {
    // get the calendar instance with the specified date
    val cal = Calendar.getInstance()
    cal.setTime(date)

    // return the date without time
    clearTime(cal)
  }

  /**
   * Generates a future expiration time
   * @param duration the number of time units into the future
   * @return the resulting date
   */
  def computeFutureDate(duration: FiniteDuration): Date = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MILLISECOND, duration.toMillis.toInt)
    cal.getTime
  }

  /**
   * Generates a future expiration time
   * @param duration the number of time units into the future
   * @return the resulting date
   */
  def computeFutureDate(srcDate: Date, duration: FiniteDuration): Date = {
    val cal = Calendar.getInstance()
    cal.setTime(srcDate)
    cal.add(Calendar.MILLISECOND, duration.toMillis.toInt)
    cal.getTime
  }

  /**
   * Returns an HTTP date string representing the time of the given date object
   * @param date the given { @link Date date} object
   * @return an HTTP date string (e.g., "Thu, 01 Dec 1994 16:00:00 GMT")
   */
  def getHTTPDate(date: Date) = new SimpleDateFormat("E',' dd MMM yyyy HH:mm:ss zz").format(date)

  /**
   * Returns midnight of the current day
   * @return the { @link Date midnight date}
   */
  def getNextMidnightTime: Date = {
    val midnight = Calendar.getInstance()
    clearTime(midnight)
    midnight.add(Calendar.DAY_OF_YEAR, 1)
    midnight.getTime
  }

  /**
   * Returns midnight of the current day
   * @return the { @link Date midnight date}
   */
  def getMidnightTime(date: Date): Date = {
    val midnight = Calendar.getInstance()
    midnight.setTime(date)
    clearTime(midnight)
    midnight.add(Calendar.DAY_OF_YEAR, 1)
    midnight.getTime
  }

  /**
   * The time in milliseconds until the next trading day
   */
  def getDelayUntilTradingStartInMillis = getTradeStartTime.getTime - (new Date).getTime

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getDaysStartTime = clearTime(new Date())

  /**
   * The U.S. Stock Markets close at 4:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getDaysEndTime = new DateTime(getDaysStartTime).plusDays(1).toDate

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getEndStartTime: Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))

    // set the time to 9:30am
    cal.set(HOUR_OF_DAY, 9)
    cal.set(MINUTE, 30)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime
  }

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getLastTradeStartTime: Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))

    // set the time to 9:30am
    cal.set(HOUR_OF_DAY, 9)
    cal.set(MINUTE, 30)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)

    // is this a week day?
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    if (dayOfWeek < MONDAY || dayOfWeek > FRIDAY) {
      if (dayOfWeek == SATURDAY) cal.add(DAY_OF_YEAR, -1)
      else if (dayOfWeek == SUNDAY) cal.add(DAY_OF_YEAR, -2)
    }

    cal.getTime
  }

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getLastTradeStopTime: Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))

    // set the time to 4:00pm
    cal.set(HOUR_OF_DAY, 16)
    cal.set(MINUTE, 0)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)

    // is this a week day?
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    if (dayOfWeek < MONDAY || dayOfWeek > FRIDAY) {
      if (dayOfWeek == SATURDAY) cal.add(DAY_OF_YEAR, -1)
      else if (dayOfWeek == SUNDAY) cal.add(DAY_OF_YEAR, -2)
    }

    cal.getTime
  }

  def getNextTradeStartTime: Date = {
    val startTime = getTradeStartTime
    if (startTime.before(new Date())) {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DAY_OF_YEAR, 1)
      getTradeStartTime(cal.getTime)
    } else startTime
  }

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getTradeStartTime: Date = getTradeStartTime(new Date())

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getTradeStartTime(time: Date): Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTime(time)
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))

    // is this a week day?
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    if (dayOfWeek == SATURDAY) cal.add(DAY_OF_YEAR, 2)
    else if (dayOfWeek == SUNDAY) cal.add(DAY_OF_YEAR, 1)

    // set the time to 9:30am
    cal.set(HOUR_OF_DAY, 9)
    cal.set(MINUTE, 30)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime
  }

  /**
   * The U.S. Stock Markets close at 4pm Eastern Time
   * @return the { @link Date stock market closing time}
   */
  def getTradeStopTime: Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    cal.setTime(getTradeStartTime)

    // set the time to 4pm
    cal.set(HOUR_OF_DAY, 16)
    cal.set(MINUTE, 0)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime
  }

  /**
   * The U.S. Stock Markets close at 4pm Eastern Time
   * @return the { @link Date stock market closing time}
   */
  def getTradeStopTime(date: Date): Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    cal.setTime(getTradeStartTime)

    // set the time to 4pm
    cal.set(HOUR_OF_DAY, 16)
    cal.set(MINUTE, 0)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime
  }

  /**
   * The U.S. Stock Markets open at 9:30am Eastern Time
   * @return the { @link Date stock market opening time}
   */
  def getLastTradingMidnight: Date = {
    import Calendar._

    // get current time in New York
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))

    // set the time to 4:30pm the day before
    cal.add(DAY_OF_YEAR, -1)
    cal.set(HOUR_OF_DAY, 0)
    cal.set(MINUTE, 0)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)

    // is this a week day?
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    if (dayOfWeek < MONDAY || dayOfWeek > FRIDAY) {
      if (dayOfWeek == SATURDAY) cal.add(DAY_OF_YEAR, 2)
      else if (dayOfWeek == SUNDAY) cal.add(DAY_OF_YEAR, 1)
    }

    cal.getTime
  }

  def isDaylightSavings: Boolean = isDaylightSavings(new Date())

  def isDaylightSavings(date: Date): Boolean = TimeZone.getDefault.inDaylightTime(date)

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
  def isTradingActive(date: Date): Boolean = isTradingActive(date.getTime)

  /**
   * Trading in the U.S. occurs between 9am and 4pm Eastern Time.
   * This method returns true if the specified time is between those hours.
   * @param timeInMillis the given time in milliseconds
   * @return true, if the specified time is between 9:30am and 4pm Eastern Time.
   */
  def isTradingActive(timeInMillis: Long): Boolean = {
    import Calendar._

    // get a calendar instance with the specified time
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    cal.setTimeInMillis(timeInMillis)

    // the day must be between Monday and Friday
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    if (dayOfWeek < MONDAY || dayOfWeek > FRIDAY) false
    else {
      // get the market start and end times
      val startTime = getTradeStartTime.getTime
      val endTime = getTradeStopTime.getTime

      // test whether the condition is satisfied
      (timeInMillis >= startTime) && (timeInMillis <= endTime)
    }
  }

  /**
   * Indicates whether the current time falls after trading hours
   * @return true, if the current time falls after trading hours
   */
  def isAfterHours: Boolean = isAfterHours(System.currentTimeMillis())

  /**
   * Indicates whether the current time falls after trading hours
   * @param timeInMillis the given time in milliseconds
   * @return true, if the current time falls after trading hours
   */
  def isAfterHours(timeInMillis: Long): Boolean = {
    import Calendar._

    // get a calendar instance with the specified time
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    cal.setTimeInMillis(timeInMillis)

    // the day must be between Monday and Friday
    val hourOfDay = cal.get(HOUR_OF_DAY)
    hourOfDay >= 16 && hourOfDay <= 20
  }

  /**
   * Indicates whether the current time falls on a weekday
   * @return true, if the current time falls on a weekday
   */
  def isWeekDay: Boolean = isWeekDay(System.currentTimeMillis())

  /**
   * Indicates whether the given time falls on a weekday
   * @param date the given { @link Date date/time}
   * @return true, if the given time falls on a weekday
   */
  def isWeekDay(date: Date): Boolean = isWeekDay(date.getTime)

  /**
   * Indicates whether the given time falls on a weekday
   * @param timeInMillis the given time in milliseconds
   * @return true, if the given time falls on a weekday
   */
  def isWeekDay(timeInMillis: Long): Boolean = !isWeekEnd(timeInMillis)

  /**
   * Indicates whether the current time falls on a weekend (Saturday or Sunday)
   * @return true, if the current time falls on a weekend (Saturday or Sunday)
   */
  def isWeekEnd: Boolean = isWeekEnd(System.currentTimeMillis())

  /**
   * Indicates whether the current time falls on a weekend (Saturday or Sunday)
   * @param date the given { @link Date date/time}
   * @return true, if the current time falls on a weekend (Saturday or Sunday)
   */
  def isWeekEnd(date: Date): Boolean = isWeekEnd(date.getTime)

  /**
   * Indicates whether the given time falls on a weekend (Saturday or Sunday)
   * @param timeInMillis the given time in milliseconds
   * @return true, if the given time falls on a weekend (Saturday or Sunday)
   */
  def isWeekEnd(timeInMillis: Long): Boolean = {
    import Calendar._

    // get a calendar instance with the specified time
    val cal = Calendar.getInstance()
    cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    cal.setTimeInMillis(timeInMillis)

    // the day must be between Monday and Friday
    val dayOfWeek = cal.get(DAY_OF_WEEK)
    dayOfWeek == SATURDAY || dayOfWeek == SUNDAY
  }

  /**
   * Provides dynamic conversion from JodaTime to java.uil.Date
   */
  implicit def jodaTime2JavaDate(time: DateTime): Date = time.toDate

  /**
   * Provides dynamic conversion from java.uil.Date to JodaTime
   */
  implicit def javaDate2JodaTime(time: Date): DateTime = new DateTime(time)

  /**
   * Implicit conversion for dates
   * @author lawrence.daniels@gmail.com
   */
  implicit class JavaDateEnrichment(date: Date) {

    def >(when: Date): Boolean = date.after(when)

    def >=(when: Date): Boolean = date.equals(when) || date.after(when)

    def <(when: Date): Boolean = date.before(when)

    def <=(when: Date): Boolean = date.equals(when) || date.before(when)

    def toJoda = new DateTime(date)

  }

  /**
   * Implicit conversion for JodaTime dates
   * @author lawrence.daniels@gmail.com
   */
  implicit class JodaTimeEnrichment(date: DateTime) {

    def >(when: Date): Boolean = date.isAfter(when.getTime)

    def >=(when: Date): Boolean = date.toDate.equals(when) || date.isAfter(when.getTime)

    def <(when: Date): Boolean = date.isBefore(when.getTime)

    def <=(when: Date): Boolean = date.toDate.equals(when) || date.isBefore(when.getTime)

    def >(when: DateTime): Boolean = date.isAfter(when)

    def >=(when: DateTime): Boolean = date.equals(when) || date.isAfter(when)

    def <(when: DateTime): Boolean = date.isBefore(when)

    def <=(when: DateTime): Boolean = date.equals(when) || date.isBefore(when)

  }

}