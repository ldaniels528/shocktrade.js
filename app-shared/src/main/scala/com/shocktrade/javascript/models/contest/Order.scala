package com.shocktrade.javascript.models.contest

import java.util.UUID

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Order Model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Order(var _id: js.UndefOr[String] = UUID.randomUUID().toString,
            var symbol: js.UndefOr[String],
            var accountType: js.UndefOr[String],
            var orderType: js.UndefOr[String],
            var priceType: js.UndefOr[String],
            var price: js.UndefOr[Double],
            var quantity: js.UndefOr[Double],
            var commission: js.UndefOr[Double],
            var creationTime: js.UndefOr[Double],
            var expirationTime: js.UndefOr[Double],
            var statusMessage: js.UndefOr[String]) extends js.Object

/**
  * Order Companion
  * @author lawrence.daniels@gmail.com
  */
object Order {

  /**
    * Order Enrichment
    * @param order the given [[Order order]]
    */
  implicit class OrderEnrichment(val order: Order) extends AnyVal {

    @inline
    def copy(_id: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             accountType: js.UndefOr[String] = js.undefined,
             orderType: js.UndefOr[String] = js.undefined,
             priceType: js.UndefOr[String] = js.undefined,
             price: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             commission: js.UndefOr[Double] = js.undefined,
             creationTime: js.UndefOr[Double] = js.undefined,
             expirationTime: js.UndefOr[Double] = js.undefined,
             statusMessage: js.UndefOr[String] = js.undefined) = {
      new Order(_id = _id ?? order._id,
        symbol = symbol ?? order.symbol,
        accountType = accountType ?? order.accountType,
        orderType = orderType ?? order.orderType,
        priceType = priceType ?? order.priceType,
        price = price ?? order.price,
        quantity = quantity ?? order.quantity,
        commission = commission ?? order.commission,
        creationTime = creationTime ?? order.creationTime,
        expirationTime = expirationTime ?? order.expirationTime,
        statusMessage = statusMessage ?? order.statusMessage)
    }

    @inline
    def isExpired(asOfTime: Double) = order.expirationTime.exists(_ > asOfTime)

    @inline
    def isCashAccount = order.accountType.contains("CASH")

    @inline
    def isMarginAccount = order.accountType.contains("MARGIN")

    @inline
    def isBuyOrder = order.orderType.contains("BUY")

    @inline
    def isSellOrder = order.orderType.contains("SELL")

    @inline
    def isLimitOrder = order.priceType.contains("LIMIT")

    @inline
    def isMarketOrder = order.priceType.contains("MARKET")

    @inline
    def isMarketAtCloseOrder = order.priceType.contains("MARKET_AT_CLOSE")

    @inline
    def totalCost = for {
      price <- order.price
      quantity <- order.quantity
      commission <- order.commission
    } yield price * quantity + commission

  }

}