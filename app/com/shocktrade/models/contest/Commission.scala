package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.json.Json.{obj => JS, _}

/**
 * Represents a trading commission
 * @author lawrence.daniels@gmail.com
 */
case class Commission(paid: BigDecimal, paidDate: Date, orderType: OrderType) {

  def toJson = JS(
    "paid" -> paid,
    "paidDate" -> paidDate,
    "orderType" -> orderType.name
  )

}
