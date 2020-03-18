package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest._

import scala.scalajs.js

/**
 * Represents a Portfolio model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Portfolio(var portfolioID: js.UndefOr[String] = js.undefined,
                var contestID: js.UndefOr[String] = js.undefined,
                var userID: js.UndefOr[String] = js.undefined,
                var username: js.UndefOr[String] = js.undefined,
                var active: js.UndefOr[Boolean] = js.undefined,
                var funds: js.UndefOr[Double] = js.undefined,
                var balance: js.UndefOr[PortfolioBalance] = js.undefined,
                var perks: js.UndefOr[js.Array[String]] = js.undefined,
                var orders: js.UndefOr[js.Array[Order]] = js.undefined,
                var closedOrders: js.UndefOr[js.Array[Order]] = js.undefined,
                var performance: js.UndefOr[js.Array[Performance]] = js.undefined,
                var positions: js.UndefOr[js.Array[Position]] = js.undefined) extends js.Object
