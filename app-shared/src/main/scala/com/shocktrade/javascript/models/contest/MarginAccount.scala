package com.shocktrade.javascript.models.contest

import scala.scalajs.js

@js.native
trait MarginAccount extends js.Object {
  var cashFunds: js.UndefOr[Double] = js.native
  var borrowedFunds: js.UndefOr[Double] = js.native
  var initialMargin: js.UndefOr[Double] = js.native
  var interestPaid: js.UndefOr[Double] = js.native
  var asOfDate: js.UndefOr[js.Date] = js.native
}