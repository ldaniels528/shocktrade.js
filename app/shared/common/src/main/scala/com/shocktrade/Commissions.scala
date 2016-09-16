package com.shocktrade

import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.OrderLike

/**
  * Commissions Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Commissions {

  def apply(form: NewOrderForm) = {
    if (form.isLimitOrder) 14.99
    else if (form.isMarketAtCloseOrder) 9.99
    else 7.99
  }

  def apply(order: OrderLike) = {
    if (order.isLimitOrder) 14.99
    else if (order.isMarketAtCloseOrder) 9.99
    else 7.99
  }

}
