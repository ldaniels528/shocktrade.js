package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Key Statistics
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait KeyStatistics extends js.Object {
  var change52Week: js.UndefOr[Double] = js.native
  var change52WeekHigh: js.UndefOr[Double] = js.native
  var change52WeekLow: js.UndefOr[Double] = js.native
  var change52WeekSNP500: js.UndefOr[Double] = js.native
}

/**
  * Key Statistics Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object KeyStatistics {
  val Fields = List(
    "change52Week", "change52WeekHigh", "change52WeekLow", "change52WeekSNP500"
  )

}