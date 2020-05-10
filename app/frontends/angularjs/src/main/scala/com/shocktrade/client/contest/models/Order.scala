package com.shocktrade.client.contest.models

import com.shocktrade.common.Commissions
import com.shocktrade.common.models.contest.OrderLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an Order model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Order(val orderID: js.UndefOr[String],
            val symbol: js.UndefOr[String],
            val exchange: js.UndefOr[String],
            val orderType: js.UndefOr[String],
            val priceType: js.UndefOr[String],
            val price: js.UndefOr[Double],
            val lastTrade: js.UndefOr[Double],
            val quantity: js.UndefOr[Double],
            val creationTime: js.UndefOr[js.Date],
            val expirationTime: js.UndefOr[js.Date],
            val processedTime: js.UndefOr[js.Date],
            val message: js.UndefOr[String],
            val closed: js.UndefOr[Int]) extends OrderLike

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