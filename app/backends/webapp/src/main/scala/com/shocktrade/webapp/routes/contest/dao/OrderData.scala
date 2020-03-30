package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.models.contest.OrderLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an Order data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrderData(val orderID: js.UndefOr[String] = UUID.randomUUID().toString,
                val symbol: js.UndefOr[String],
                val exchange: js.UndefOr[String],
                val orderType: js.UndefOr[String],
                val priceType: js.UndefOr[String],
                val price: js.UndefOr[Double],
                val quantity: js.UndefOr[Double],
                val creationTime: js.UndefOr[js.Date],
                val expirationTime: js.UndefOr[js.Date] = js.undefined,
                val processedTime: js.UndefOr[js.Date] = js.undefined,
                val message: js.UndefOr[String] = js.undefined,
                val fulfilled: js.UndefOr[Boolean] = js.undefined,
                val closed: js.UndefOr[Boolean] = js.undefined) extends OrderLike

/**
 * Order Data Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderData {

  /**
   * Order Data Enrichment
   * @param order the given [[OrderData order]]
   */
  final implicit class OrderEnrichment(val order: OrderData) extends AnyVal {

    @inline
    def toClosedOrder(statusMessage: String, asOfDate: js.Date = new js.Date()): OrderData = order.copy(
      statusMessage = statusMessage,
      processedTime = asOfDate
    )

    @inline
    def copy(orderID: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             accountType: js.UndefOr[String] = js.undefined,
             orderType: js.UndefOr[String] = js.undefined,
             priceType: js.UndefOr[String] = js.undefined,
             price: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             creationTime: js.UndefOr[js.Date] = js.undefined,
             expirationTime: js.UndefOr[js.Date] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined,
             statusMessage: js.UndefOr[String] = js.undefined,
             fulfilled: js.UndefOr[Boolean] = js.undefined,
             closed: js.UndefOr[Boolean] = js.undefined): OrderData = {
      new OrderData(
        orderID = orderID ?? order.orderID,
        symbol = symbol ?? order.symbol,
        exchange = exchange ?? order.exchange,
        orderType = orderType ?? order.orderType,
        priceType = priceType ?? order.priceType,
        price = price ?? order.price,
        quantity = quantity ?? order.quantity,
        creationTime = creationTime ?? order.creationTime,
        expirationTime = expirationTime ?? order.expirationTime,
        processedTime = processedTime ?? order.processedTime,
        message = statusMessage ?? order.message,
        fulfilled = fulfilled ?? order.fulfilled,
        closed = closed ?? order.closed)
    }

  }

}