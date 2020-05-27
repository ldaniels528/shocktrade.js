package com.shocktrade.common.forms

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * New Order Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewOrderForm(var portfolioID: js.UndefOr[String],
                   var symbol: js.UndefOr[String],
                   var exchange: js.UndefOr[String],
                   var orderType: js.UndefOr[String],
                   var orderTerm: js.UndefOr[String],
                   var priceType: js.UndefOr[String],
                   var quantity: js.UndefOr[Double],
                   var limitPrice: js.UndefOr[Double],
                   var perks: js.UndefOr[js.Array[String]],
                   var emailNotify: js.UndefOr[Boolean]) extends js.Object

/**
 * New Order Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewOrderForm {

  def apply(portfolioID: js.UndefOr[String] = js.undefined,
            symbol: js.UndefOr[String] = js.undefined,
            exchange: js.UndefOr[String] = js.undefined,
            orderType: js.UndefOr[String] = js.undefined,
            orderTerm: js.UndefOr[String] = js.undefined,
            priceType: js.UndefOr[String] = js.undefined,
            quantity: js.UndefOr[Double] = js.undefined,
            limitPrice: js.UndefOr[Double] = js.undefined,
            perks: js.UndefOr[js.Array[String]] = js.undefined,
            emailNotify: js.UndefOr[Boolean] = js.undefined): NewOrderForm = {
    new NewOrderForm(
      portfolioID = portfolioID,
      symbol = symbol,
      exchange = exchange,
      orderType = orderType,
      orderTerm = orderTerm,
      priceType = priceType,
      quantity = quantity,
      limitPrice = limitPrice,
      perks = perks,
      emailNotify = emailNotify
    )
  }

  /**
   * New Order Form Enrichment
   * @param form the given [[NewOrderForm form]]
   */
  final implicit class NewOrderFormEnrichment(val form: NewOrderForm) extends AnyVal {

    @inline
    def isBuyOrder: Boolean = form.orderType.contains("BUY")

    @inline
    def isSellOrder: Boolean = form.orderType.contains("SELL")

    @inline
    def isLimitOrder: Boolean = form.priceType.contains("LIMIT")

    @inline
    def isMarketOrder: Boolean = form.priceType.contains("MARKET")

    @inline
    def totalCost: js.UndefOr[Double] = for (price <- form.limitPrice; qty <- form.quantity) yield price * qty

    @inline
    def validate: js.Array[String] = {
      val messages = js.Array[String]()
      if (form.symbol.nonAssigned) messages.append("Symbol is required")
      if (form.orderTerm.nonAssigned) messages.push("No Order Term specified")
      if (form.orderType.nonAssigned) messages.push("No Order Type (BUY or SELL) specified")
      if (form.priceType.nonAssigned) messages.push("No Pricing Method specified")
      if (form.quantity.nonAssigned || form.quantity.exists(_ <= 0)) messages.push("No quantity specified")
      if (form.isLimitOrder && form.limitPrice.nonAssigned) messages.append("Price is required for Limit orders")
      if (!form.isBuyOrder && !form.isSellOrder) messages.append("BUY or SELL is required")
      messages
    }

  }

}