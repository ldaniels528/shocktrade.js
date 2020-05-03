package com.shocktrade.common

/**
 * Order Constants
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderConstants {
  // order types
  type OrderType = String
  val BUY: OrderType = "BUY"
  val SELL: OrderType = "SELL"

  // order terms
  type OrderTerm = Int
  val GOOD_FOR_DAY: OrderTerm = 1
  val GOOD_FOR_3_DAYS: OrderTerm = 3
  val GOOD_FOR_7_DAYS: OrderTerm = 7
  val GOOD_FOR_14_DAYS: OrderTerm = 14
  val GOOD_FOR_30_DAYS: OrderTerm = 30
  val GOOD_FOR_60_DAYS: OrderTerm = 60
  val GOOD_UNTIL_CANCELED: OrderTerm = 365

  // price types
  type PriceType = String
  val Limit: PriceType = "LIMIT"
  val Market: PriceType = "MARKET"
  val MarketAtClose: PriceType = "MARKET_AT_CLOSE"

}
