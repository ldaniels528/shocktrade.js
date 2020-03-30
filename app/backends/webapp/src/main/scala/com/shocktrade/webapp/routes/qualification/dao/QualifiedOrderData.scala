package com.shocktrade.webapp.routes.qualification.dao

import scala.scalajs.js

class QualifiedOrderData(val contestID: js.UndefOr[String],
                         val userID: js.UndefOr[String],
                         val portfolioID: js.UndefOr[String],
                         val orderID: js.UndefOr[String],
                         val symbol: js.UndefOr[String],
                         val exchange: js.UndefOr[String],
                         val orderType: js.UndefOr[String],
                         val priceType: js.UndefOr[String],
                         val price: js.UndefOr[Double],
                         val quantity: js.UndefOr[Double],
                         val lastTrade: js.UndefOr[Double],
                         val tradeDateTime: js.UndefOr[js.Date],
                         val cost: js.UndefOr[Double],
                         val funds: js.UndefOr[Double],
                         val creationTime: js.UndefOr[js.Date],
                         val expirationTime: js.UndefOr[js.Date]) extends js.Object