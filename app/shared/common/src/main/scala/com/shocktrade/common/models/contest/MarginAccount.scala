package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a margin account
  * @param funds         the amount of cash contained within the account
  * @param initialMargin the initial margin
  * @param interestPaid  the total amount of interest paid
  * @param asOfDate      the account's effective date
  */
@ScalaJSDefined
class MarginAccount(var funds: js.UndefOr[Double],
                    var initialMargin: js.UndefOr[Double],
                    var interestPaid: js.UndefOr[Double],
                    var asOfDate: js.UndefOr[js.Date]) extends js.Object