package com.shocktrade.common.forms

import com.shocktrade.common.forms.OrderSearchOptions.OrderStatus

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Order Search Options
 * @param contestID   the given contest ID
 * @param userID      the given user ID
 * @param portfolioID the given portfolio ID
 * @param orderID     the given order ID
 * @param orderType   the given order type
 * @param status      the given status
 */
class OrderSearchOptions(var contestID: js.UndefOr[String],
                         var userID: js.UndefOr[String],
                         var orderID: js.UndefOr[String],
                         var portfolioID: js.UndefOr[String],
                         var orderType: js.UndefOr[String],
                         var status: js.UndefOr[OrderStatus]) extends js.Object

/**
 * Order Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderSearchOptions {
  type OrderStatus = Int
  val ACTIVE_ORDERS: OrderStatus = 0
  val COMPLETED_ORDERS: OrderStatus = 1
  val FULFILLED_ORDERS: OrderStatus = 2
  val FAILED_ORDERS: OrderStatus = 3

  def apply(contestID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            orderID: js.UndefOr[String] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined,
            orderType: js.UndefOr[String] = js.undefined,
            status: js.UndefOr[OrderStatus] = js.undefined): OrderSearchOptions = {
    new OrderSearchOptions(
      contestID = contestID,
      orderID = orderID,
      portfolioID = portfolioID,
      userID = userID,
      orderType = orderType,
      status = status
    )
  }

  /**
   * Order Search Options Enrichment
   * @param options the given [[OrderSearchOptions]]
   */
  final implicit class OrderSearchOptionsEnrichment(val options: OrderSearchOptions) extends AnyVal {
    @inline
    def toQueryString: String = {
      val values = options.asInstanceOf[js.Dictionary[js.UndefOr[Any]]].toJSArray.filter { case (_, v) => v.nonEmpty }
      (for ((name, value) <- values) yield s"$name=$value").mkString("&")
    }
  }

}