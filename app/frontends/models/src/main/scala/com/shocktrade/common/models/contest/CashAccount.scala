package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
  * Represents a cash account
  * @param funds    the amount of cash contained within the account
  * @param asOfDate the account's effective date
  */
class CashAccount(var funds: js.UndefOr[Double], var asOfDate: js.UndefOr[js.Date]) extends js.Object