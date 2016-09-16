package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a margin account
  * @param cashFunds     the amount of cash contained within the account
  * @param borrowedFunds the amount of levered funds used by the account
  * @param initialMargin the initial margin
  * @param interestPaid  the total amount of interest paid
  * @param asOfDate      the account's effective date
  */
@ScalaJSDefined
class MarginAccount(var cashFunds: js.UndefOr[Double] = js.undefined,
                    var borrowedFunds: js.UndefOr[Double] = js.undefined,
                    var initialMargin: js.UndefOr[Double] = js.undefined,
                    var interestPaid: js.UndefOr[Double] = js.undefined,
                    var asOfDate: js.UndefOr[js.Date] = js.undefined) extends js.Object 