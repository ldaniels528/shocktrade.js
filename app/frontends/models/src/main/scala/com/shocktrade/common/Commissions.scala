package com.shocktrade.common

import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.OrderLike

import scala.scalajs.js

/**
 * Commissions Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Commissions {
  val LIMIT_COST = 9.99
  val MARKET_COST = 7.99

  def apply(form: NewOrderForm): Double = {
    if (form.isLimitOrder) LIMIT_COST
    else MARKET_COST
  }

  def apply(order: OrderLike): Double = {
    if (order.isLimitOrder) LIMIT_COST
    else MARKET_COST
  }

  def getCommission(priceType: js.UndefOr[String]): Double = {
    priceType map {
      case "LIMIT" => LIMIT_COST
      case "MARKET" => MARKET_COST
      case _ => LIMIT_COST
    } getOrElse LIMIT_COST
  }

}
