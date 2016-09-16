package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Portfolio model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class Portfolio(var _id: js.UndefOr[String] = js.undefined,
                var contestID: js.UndefOr[String] = js.undefined,
                var playerID: js.UndefOr[String] = js.undefined,
                var perks: js.UndefOr[js.Array[String]] = js.undefined,
                var cashAccount: js.UndefOr[CashAccount] = js.undefined,
                var marginAccount: js.UndefOr[MarginAccount] = js.undefined,
                var orders: js.UndefOr[js.Array[Order]] = js.undefined,
                var closedOrders: js.UndefOr[js.Array[Order]] = js.undefined,
                var performance: js.UndefOr[js.Array[Performance]] = js.undefined,
                var positions: js.UndefOr[js.Array[Position]] = js.undefined) extends PortfolioLike
