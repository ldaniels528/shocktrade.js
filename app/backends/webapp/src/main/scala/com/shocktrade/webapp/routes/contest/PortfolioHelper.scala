package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.webapp.routes.contest.dao.OrderData
import io.scalajs.util.DateHelper._

import scala.concurrent.duration._
import scala.scalajs.js
import scala.util.Try

object PortfolioHelper {

  /**
   * New Order Form Extensions
   * @param form the given [[NewOrderForm form]]
   */
  implicit class NewOrderFormExtensions(val form: NewOrderForm) extends AnyVal {

    @inline
    def toOrder: OrderData = {
      new OrderData(
        orderID = js.undefined,
        symbol = form.symbol,
        exchange = form.exchange,
        orderType = form.orderType,
        priceType = form.priceType,
        price = if (form.isLimitOrder) form.limitPrice else js.undefined,
        quantity = form.quantity,
        creationTime = new js.Date(),
        expirationTime = form.orderTerm.map(s => new js.Date() + Try(s.toInt).getOrElse(3).days),
        processedTime = js.undefined,
        message = js.undefined)
    }
  }

}
