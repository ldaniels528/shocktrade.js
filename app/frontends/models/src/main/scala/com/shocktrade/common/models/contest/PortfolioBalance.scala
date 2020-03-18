package com.shocktrade.common.models.contest

import scala.scalajs.js

class PortfolioBalance(val contestID: js.UndefOr[String],
                       val name: js.UndefOr[String],
                       val userID: js.UndefOr[String],
                       val username: js.UndefOr[String],
                       val wallet: js.UndefOr[Double],
                       val funds: js.UndefOr[Double],
                       val totalBuyOrders: js.UndefOr[Double],
                       val totalSellOrders: js.UndefOr[Double],
                       val equity: js.UndefOr[Double]) extends js.Object