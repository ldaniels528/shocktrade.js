package com.shocktrade.javascript

import biz.enef.angulate.AnnotatedFunction
import com.shocktrade.javascript.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Any._
import scala.scalajs.js.Dynamic.{global => g}

/**
 * ShockTrade Filters
 * @author lawrence.daniels@gmail.com
 */
object Filters {

  /**
   * Absolute Value
   */
  val abs: AnnotatedFunction = () => { (value: js.UndefOr[Double]) =>
    value map { v => String.valueOf(Math.abs(v))} getOrElse ""
  } :js.Function

  /**
   * Big Number: Formats large numbers into as a compact expression (e.g. "1.2M")
   */
  val bigNumber: AnnotatedFunction = () => { value: js.UndefOr[Double] =>
    value map {
      case num if Math.abs(num) >= 1.0e+12 => f"${num / 1.0e+12}%.2fT"
      case num if Math.abs(num) >= 1.0e+9 => f"${num / 1.0e+9}%.2fB"
      case num if Math.abs(num) >= 1.0e+6 => f"${num / 1.0e+6}%.2fM"
      case num if Math.abs(num) <= 1.0e+3 => f"${num / 1.0e+3}%.2fK"
      case num => f"$num%.2f"
    } getOrElse ""
  }: js.Function

  /**
   * Capitalize: Returns the capitalize representation of a given string
   */
  val capitalize: AnnotatedFunction = () => { (s: String) =>
    if (s.nonEmpty) s.head.toUpper + s.tail else ""
  }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val duration: AnnotatedFunction = () => { (time: js.Dynamic) => toDuration(time, noFuture = false) }: js.Function

  /**
   * Escape: Performs an escape
   */
  val escape: AnnotatedFunction = () => { () => g.window.escape }: js.Function

  /**
   * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
   */
  val newsDuration: AnnotatedFunction = () => { (time: js.Dynamic) => toDuration(time, noFuture = true) }: js.Function

  /**
   * Quote Change: Formats the change percent property of a quote (e.g. 1.2")
   */
  val quoteChange: AnnotatedFunction = () => { value: js.UndefOr[Double] =>
    value map {
      case num if Math.abs(num) >= 100 => f"$num%.0f"
      case num if Math.abs(num) >= 10 => f"$num%.1f"
      case num => f"$num%.2f"
    } getOrElse ""
  }: js.Function

  /**
   * Quote Number: Formats an amount to provide the best display accuracy (e.g. "100.20" or "0.0001")
   */
  val quoteNumber: AnnotatedFunction = () => { value: js.UndefOr[Double] =>
    value map {
      case num if Math.abs(num) < 0.0001 => f"$num%.5f"
      case num if Math.abs(num) < 10 => f"$num%.4f"
      case num => f"$num%.2f"
    } getOrElse ""
  }: js.Function

  /**
   * Yes/No: Converts a boolean value into 'Yes' or 'No'
   */
  val yesNo: AnnotatedFunction = () => ((state: Boolean) => if (state) "Yes" else "No"): js.Function

  /**
   * Converts the given time expression to a textual duration
   * @param time the given time stamp (in milliseconds)
   * @return the duration (e.g. "10 mins ago")
   */
  def toDuration(time: js.Dynamic, noFuture: Boolean = false) = {
    // get an option of the time
    val myTime = {
      if (!isDefined(time)) Option(js.Date.now())
      else if (isDefined(time.$date)) time.$date.asOpt[Double]
      else time.asOpt[Double]
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

  private val timeUnits = Seq("min", "hour", "day", "month", "year")
  private val timeFactors = Seq(60, 24, 30, 12)

}
