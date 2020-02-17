package com.shocktrade.serverside.persistence.eventsource

/**
  * Represents an enumeration of price types
  * @author lawrence.daniels@gmail.com
  */
object PriceTypes {
  type PriceType = String

  val Limit: PriceType = "LIMIT"
  val Market: PriceType = "MARKET"
  val MarketAtClose: PriceType = "MARKET_AT_CLOSE"

}