package com.shocktrade.javascript.models.contest

import scala.scalajs.js

/**
  * Cash Account Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait CashAccount extends js.Object {
  var cashFunds: js.UndefOr[Double] = js.native
  var asOfDate: js.UndefOr[js.Date] = js.native
}
