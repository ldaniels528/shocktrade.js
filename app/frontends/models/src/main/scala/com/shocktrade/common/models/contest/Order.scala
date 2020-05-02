package com.shocktrade.common.models.contest

import com.shocktrade.common.Commissions
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an Order model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Order(var orderID: js.UndefOr[String],
            var symbol: js.UndefOr[String],
            var exchange: js.UndefOr[String],
            var orderType: js.UndefOr[String],
            var priceType: js.UndefOr[String],
            var price: js.UndefOr[Double],
            var quantity: js.UndefOr[Double],
            var creationTime: js.UndefOr[js.Date],
            var expirationTime: js.UndefOr[js.Date],
            var processedTime: js.UndefOr[js.Date],
            var message: js.UndefOr[String],
            var closed: js.UndefOr[Int]) extends OrderLike {

  // UI-specific fields
  var lastTrade: js.UndefOr[Double] = js.undefined

}

/**
 * Order Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Order {

  /**
   * Order Enrichment
   * @param order the given [[Order order]]
   */
  final implicit class OrderEnrichment(val order: Order) extends AnyVal {

    @inline
    def totalCost: js.UndefOr[Double] = for {
      price <- order.price ?? order.lastTrade
      quantity <- order.quantity
    } yield price * quantity + Commissions(order)
  }

}