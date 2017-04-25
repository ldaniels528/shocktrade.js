package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest.OrderLike

import scala.scalajs.js

/**
  * Represents an Order model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Order(var _id: js.UndefOr[String],
            var symbol: js.UndefOr[String],
            var exchange: js.UndefOr[String],
            var accountType: js.UndefOr[String],
            var orderType: js.UndefOr[String],
            var priceType: js.UndefOr[String],
            var price: js.UndefOr[Double],
            var quantity: js.UndefOr[Double],
            var creationTime: js.UndefOr[js.Date],
            var expirationTime: js.UndefOr[js.Date],
            var processedTime: js.UndefOr[js.Date],
            var statusMessage: js.UndefOr[String]) extends OrderLike {

  // UI-specific fields
  var lastTrade: js.UndefOr[Double] = js.undefined

}