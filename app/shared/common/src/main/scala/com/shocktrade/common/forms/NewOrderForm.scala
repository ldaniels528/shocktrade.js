package com.shocktrade.common.forms

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * New Order Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewOrderForm(var symbol: js.UndefOr[String] = js.undefined,
                   var exchange: js.UndefOr[String] = js.undefined,
                   var accountType: js.UndefOr[String] = js.undefined,
                   var orderType: js.UndefOr[String] = js.undefined,
                   var orderTerm: js.UndefOr[String] = js.undefined,
                   var priceType: js.UndefOr[String] = js.undefined,
                   var quantity: js.UndefOr[Double] = js.undefined,
                   var limitPrice: js.UndefOr[Double] = js.undefined,
                   var perks: js.UndefOr[js.Array[String]] = js.undefined,
                   var emailNotify: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
  * New Order Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewOrderForm {

  /**
    * New Order Form Enrichment
    * @param form the given [[NewOrderForm form]]
    */
  implicit class NewOrderFormEnrichment(val form: NewOrderForm) extends AnyVal {

    @inline
    def isCashAccount: Boolean = form.accountType.contains("CASH")

    @inline
    def isMarginAccount: Boolean = form.accountType.contains("MARGIN")

    @inline
    def isBuyOrder: Boolean = form.orderType.contains("BUY")

    @inline
    def isSellOrder: Boolean = form.orderType.contains("SELL")

    @inline
    def isLimitOrder: Boolean = form.priceType.contains("LIMIT")

    @inline
    def isMarketOrder: Boolean = form.priceType.contains("MARKET")

    @inline
    def isMarketAtCloseOrder: Boolean = form.priceType.contains("MARKET_AT_CLOSE")

    @inline
    def validate: js.Array[String] = {
      val messages = js.Array[String]()
      if (form.symbol.nonAssigned) messages.append("Symbol is required")
      if (form.accountType.nonAssigned) messages.push("Please selected the account to use (Cash or Margin)")
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