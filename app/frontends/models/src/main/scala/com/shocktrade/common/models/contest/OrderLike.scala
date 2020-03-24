package com.shocktrade.common.models.contest

import com.shocktrade.common.Commissions
import io.scalajs.util.DateHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an Order-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OrderLike extends js.Object {

  def orderID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def exchange: js.UndefOr[String]

  def orderType: js.UndefOr[String]

  def priceType: js.UndefOr[String]

  def price: js.UndefOr[Double]

  def quantity: js.UndefOr[Double]

  def creationTime: js.UndefOr[js.Date]

  def expirationTime: js.UndefOr[js.Date]

  def processedTime: js.UndefOr[js.Date]

  def statusMessage: js.UndefOr[String]

}

/**
 * Order-like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderLike {

  val ORDER_TYPE_BUY = "BUY"
  val ORDER_TYPE_SELL = "SELL"

  val PRICE_TYPE_LIMIT = "LIMIT"
  val PRICE_TYPE_MARKET = "MARKET"
  val PRICE_TYPE_MARKET_AT_CLOSE = "MARKET_AT_CLOSE"

  /**
   * Order Enrichment
   * @param order the given [[OrderLike order]]
   */
  implicit class OrderLikeEnrichment(val order: OrderLike) extends AnyVal {

    @inline
    def isExpired(asOfTime: js.Date): Boolean = order.expirationTime.exists(_ < asOfTime)

    @inline
    def isBuyOrder: Boolean = order.orderType.contains(ORDER_TYPE_BUY)

    @inline
    def isSellOrder: Boolean = order.orderType.contains(ORDER_TYPE_SELL)

    @inline
    def isLimitOrder: Boolean = order.priceType.contains("LIMIT")

    @inline
    def isMarketOrder: Boolean = order.priceType.contains("MARKET")

    @inline
    def isMarketAtCloseOrder: Boolean = order.priceType.contains("MARKET_AT_CLOSE")

    @inline
    def totalCost: js.UndefOr[Double] = for {
      price <- order.price
      quantity <- order.quantity
    } yield price * quantity + Commissions(order)

  }

}