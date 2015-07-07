package com.shocktrade.javascript

import ScalaJsHelper._

import com.ldaniels528.scalascript.angular
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Any._
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.Try

/**
 * ShockTrade Filters
 * @author lawrence.daniels@gmail.com
 */
object Filters {
  private val timeUnits = Seq("min", "hour", "day", "month", "year")
  private val timeFactors = Seq(60, 24, 30, 12)

  /**
   * Absolute Value
   */
  val abs: js.Function = () => { (value: js.UndefOr[Any]) =>
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
  val capitalize: js.Function = () => { (value: js.UndefOr[String]) =>
    value map { s => if (s.nonEmpty) s.head.toUpper + s.tail else "" }
  }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val duration: js.Function = () => { (time: js.Dynamic) => toDuration(time, noFuture = false) }: js.Function

  /**
   * Escape: Performs an escape
   */
  val escape: js.Function = () => { () => g.window.escape }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val newsDuration: js.Function = () => { (time: js.Dynamic) => toDuration(time, noFuture = true) }: js.Function

  /**
   * Quote Change: Formats the change percent property of a quote (e.g. 1.2")
   */
  val quoteChange: js.Function = () => { value: js.UndefOr[Any] =>
    value map {
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
    value map {
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

  /**
   * Converts the given time expression to a textual duration
   * @param time the given time stamp (in milliseconds)
   * @return the duration (e.g. "10 mins ago")
   */
  def toDuration(time: js.UndefOr[js.Any], noFuture: Boolean = false) = {
    // get the time in milliseconds
    val myTime = time.toOption map {
      case value if angular.isDate(value) => value.asInstanceOf[js.Date].getTime()
      case value if angular.isNumber(value) => value.asInstanceOf[Double]
      case value if angular.isObject(value) =>
        val obj = value.asInstanceOf[js.Dynamic]
        if(angular.isDefined(obj.$date)) obj.$date.as[Double] else js.Date.now()
      case _ => js.Date.now()
    } getOrElse js.Date.now()

    // compute the elapsed time
    val elapsed = (js.Date.now() - myTime) / 60000

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
      if (noFuture) "moments ago" else f"$age%.0f $unitName from now"
    }
    else f"$age%.0f $unitName ago"
  }

}
