package com.shocktrade.javascript.data

import scala.scalajs.js

/**
  * Key Statistics
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait KeyStatistics extends js.Object {
  var change52Week: js.UndefOr[Double] = js.native
  var change52WeekHigh: js.UndefOr[Double] = js.native
  var change52WeekLow: js.UndefOr[Double] = js.native
  var change52WeekSNP500: js.UndefOr[Double] = js.native
}
