package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a cash account
  * @param funds    the amount of cash contained within the account
  * @param asOfDate the account's effective date
  */
@ScalaJSDefined
class CashAccount(var funds: js.UndefOr[Double], var asOfDate: js.UndefOr[js.Date]) extends js.Object