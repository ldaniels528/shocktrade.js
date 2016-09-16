package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a cash account
  * @param cashFunds the amount of cash contained within the account
  * @param asOfDate  the account's effective date
  */
@ScalaJSDefined
class CashAccount(var cashFunds: js.UndefOr[Double] = js.undefined,
                  var asOfDate: js.UndefOr[js.Date] = js.undefined) extends js.Object 