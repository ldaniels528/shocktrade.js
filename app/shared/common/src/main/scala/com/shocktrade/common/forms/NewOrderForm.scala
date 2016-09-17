package com.shocktrade.common.forms

import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * New Order Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
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
    def isCashAccount = form.accountType.contains("CASH")

    @inline
    def isMarginAccount = form.accountType.contains("MARGIN")

    @inline
    def isBuyOrder = form.orderType.contains("BUY")

    @inline
    def isSellOrder = form.orderType.contains("SELL")

    @inline
    def isLimitOrder = form.priceType.contains("LIMIT")

    @inline
    def isMarketOrder = form.priceType.contains("MARKET")

    @inline
    def isMarketAtCloseOrder = form.priceType.contains("MARKET_AT_CLOSE")

    @inline
    def validate = {
      val messages = js.Array[String]()
      if (form.symbol.isEmpty) messages.append("Symbol is required")
      if (form.isLimitOrder && form.limitPrice.nonAssigned) messages.append("Price is required for Limit orders")
      if (!form.isBuyOrder && !form.isSellOrder) messages.append("BUY or SELL is required")
      messages
    }

  }

}