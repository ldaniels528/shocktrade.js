package com.shocktrade.client

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.Any._
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.Try

/**
 * ShockTrade Filters
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Filters {

  /**
   * Absolute Value
   */
  val abs: js.Function = () => { value: js.UndefOr[Any] =>
    value map {
      case s: String if s.nonEmpty => Try(s.toCharArray).toOption.getOrElse(0.0d)
      case v: Number => Math.abs(v.doubleValue())
      case _ => 0.0d
    } getOrElse value
  }: js.Function

  /**
   * Big Number: Formats large numbers into as a compact expression (e.g. "1.2M")
   */
  val bigNumber: js.Function = () => { value: js.UndefOr[Any] =>
    value map {
      case s: String if s.nonEmpty => Try(s.toCharArray).toOption.getOrElse(0.0d)
      case n: Number => n.doubleValue() match {
        case num if Math.abs(num) >= 1.0e+12 => f"${num / 1.0e+12}%.2fT"
        case num if Math.abs(num) >= 1.0e+9 => f"${num / 1.0e+9}%.2fB"
        case num if Math.abs(num) >= 1.0e+6 => f"${num / 1.0e+6}%.2fM"
        case num if Math.abs(num) <= 1.0e+3 => f"${num / 1.0e+3}%.2fK"
        case num => f"$num%.2f"
      }
      case _ => 0.0d
    } getOrElse ""
  }: js.Function

  /**
   * Capitalize: Returns the capitalize representation of a given string
   */
  val capitalize: js.Function = () => { value: js.UndefOr[String] =>
    value map { s => if (s.nonEmpty) s.head.toUpper + s.tail else "" }
  }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val duration: js.Function = () => { time: js.UndefOr[js.Any] => toDuration(time, noFuture = false) }: js.Function

  /**
   * Escape: Performs an escape
   */
  val escape: js.Function = () => { () => g.window.escape }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val newsDuration: js.Function = () => { time: js.UndefOr[js.Any] => toDuration(time, noFuture = true) }: js.Function

  /**
   * Quote Change: Formats the change percent property of a quote (e.g. 1.2")
   */
  val quoteChange: js.Function = () => { value: js.UndefOr[Any] =>
    value.flat  map {
      case s: String if s.nonEmpty => s
      case n: Number => n.doubleValue() match {
        case num if Math.abs(num) >= 100 => f"$num%.0f"
        case num if Math.abs(num) >= 10 => f"$num%.1f"
        case num => f"$num%.2f"
      }
      case _ => ""
    } getOrElse ""
  }: js.Function

  /**
   * Quote Number: Formats an amount to provide the best display accuracy (e.g. "100.20" or "0.0001")
   */
  val quoteNumber: js.Function = () => { value: js.UndefOr[Any] =>
    value.flat map {
      case s: String if s.nonEmpty => s
      case n: Number => n.doubleValue() match {
        case num if Math.abs(num) < 0.0001 => f"$num%.5f"
        case num if Math.abs(num) < 10 => f"$num%.4f"
        case num => f"$num%.2f"
      }
      case _ => ""
    } getOrElse ""
  }: js.Function

  /**
   * Yes/No: Converts a boolean value into 'Yes' or 'No'
   */
  val yesNo: js.Function = () => ((state: Boolean) => if (state) "Yes" else "No"): js.Function
  private val timeUnits = Seq("min", "hour", "day", "month", "year")
  private val timeFactors = Seq(60, 24, 30, 12)

  /**
   * Converts the given time expression to a textual duration
   * @param aTime the given [[js.Date]] or time stamp (in milliseconds)
   * @return the duration (e.g. "10 mins ago")
   */
  def toDuration(aTime: js.UndefOr[js.Any], noFuture: Boolean = false): js.UndefOr[String] = aTime.flat map { time =>
    val ts = time.toString match {
      case s if s.matches("\\d+") => s.toDouble
      case s => js.Date.parse(s)
    }

    // compute the elapsed time
    val elapsed = (js.Date.now() - ts) / 60000

    // compute the age
    var age = Math.abs(elapsed)
    var unit = 0
    while (unit < timeFactors.length && age >= timeFactors(unit)) {
      age /= timeFactors(unit)
      unit += 1
    }

    // make the age and unit names more readable
    val unitName = timeUnits(unit) + (if (age.toInt != 1) "s" else "")
    if (unit == 0 && (age >= 0 && age < 1)) "just now"
    else if (elapsed < 0) {
      if (noFuture) "moments ago" else f"in $age%.0f $unitName"
    }
    else f"$age%.0f $unitName ago"
  }

}
