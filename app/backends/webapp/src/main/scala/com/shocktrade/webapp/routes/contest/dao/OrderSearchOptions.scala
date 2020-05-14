package com.shocktrade.webapp.routes.contest.dao

import scala.scalajs.js

/**
 * Order Search Options
 * @param contestID   the given contest ID
 * @param userID      the given user ID
 * @param portfolioID the given portfolio ID
 * @param orderID     the given order ID
 */
class OrderSearchOptions(val contestID: js.UndefOr[String] = js.undefined,
                         val userID: js.UndefOr[String] = js.undefined,
                         val orderID: js.UndefOr[String] = js.undefined,
                         val portfolioID: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Order Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderSearchOptions {

  def apply(contestID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            orderID: js.UndefOr[String] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined): OrderSearchOptions = {
    new OrderSearchOptions(
      contestID = contestID,
      orderID = orderID,
      portfolioID = portfolioID,
      userID = userID
    )
  }

}