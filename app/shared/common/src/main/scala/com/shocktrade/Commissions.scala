package com.shocktrade

import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.OrderLike

import scala.language.reflectiveCalls

/**
  * Commissions Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Commissions {
  val LIMIT_COST = 14.99
  val MARKET_AT_CLOSE_COST = 9.99
  val MARKET_COST = 7.99

  def apply(form: NewOrderForm) = {
    if (form.isLimitOrder) LIMIT_COST
    else if (form.isMarketAtCloseOrder) MARKET_AT_CLOSE_COST
    else MARKET_COST
  }

  def apply(order: OrderLike) = {
    if (order.isLimitOrder) LIMIT_COST
    else if (order.isMarketAtCloseOrder) MARKET_AT_CLOSE_COST
    else MARKET_COST
  }

}
