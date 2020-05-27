package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.OrderLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an Order data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrderData(val portfolioID: js.UndefOr[String],
                val orderID: js.UndefOr[String],
                val symbol: js.UndefOr[String],
                val exchange: js.UndefOr[String],
                val orderType: js.UndefOr[String],
                val priceType: js.UndefOr[String],
                val price: js.UndefOr[Double],
                val quantity: js.UndefOr[Double],
                val creationTime: js.UndefOr[js.Date],
                val expirationTime: js.UndefOr[js.Date],
                val processedTime: js.UndefOr[js.Date],
                val message: js.UndefOr[String],
                val fulfilled: js.UndefOr[Int],
                val closed: js.UndefOr[Int]) extends OrderLike

/**
 * Order Data Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderData {

  def apply(portfolioID: js.UndefOr[String] = js.undefined,
            orderID: js.UndefOr[String] = js.undefined,
            symbol: js.UndefOr[String] = js.undefined,
            exchange: js.UndefOr[String] = js.undefined,
            orderType: js.UndefOr[String] = js.undefined,
            priceType: js.UndefOr[String] = js.undefined,
            price: js.UndefOr[Double] = js.undefined,
            quantity: js.UndefOr[Double] = js.undefined,
            creationTime: js.UndefOr[js.Date] = js.undefined,
            expirationTime: js.UndefOr[js.Date] = js.undefined,
            processedTime: js.UndefOr[js.Date] = js.undefined,
            message: js.UndefOr[String] = js.undefined,
            fulfilled: js.UndefOr[Int] = js.undefined,
            closed: js.UndefOr[Int] = js.undefined): OrderData = {
    new OrderData(
      portfolioID, orderID, symbol, exchange, orderType, priceType, price, quantity,
      creationTime, expirationTime, processedTime, message, fulfilled, closed)
  }

  /**
   * Order Data Enrichment
   * @param order the given [[OrderData order]]
   */
  final implicit class OrderEnrichment(val order: OrderData) extends AnyVal {

    @inline
    def copy(portfolioID: js.UndefOr[String] = js.undefined,
             orderID: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             orderType: js.UndefOr[String] = js.undefined,
             priceType: js.UndefOr[String] = js.undefined,
             price: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             creationTime: js.UndefOr[js.Date] = js.undefined,
             expirationTime: js.UndefOr[js.Date] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined,
             statusMessage: js.UndefOr[String] = js.undefined,
             fulfilled: js.UndefOr[Int] = js.undefined,
             closed: js.UndefOr[Int] = js.undefined): OrderData = {
      new OrderData(
        portfolioID = portfolioID ?? order.portfolioID,
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

    @inline
    def toClosedOrder(statusMessage: String, asOfDate: js.Date = new js.Date()): OrderData = order.copy(
      statusMessage = statusMessage,
      processedTime = asOfDate
    )

  }

}