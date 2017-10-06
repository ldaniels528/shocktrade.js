package com.shocktrade.serverside.persistence.eventsource

object OrderTypes {
  type OrderType = String

  val Buy: OrderType = "BUY"
  val Sell: OrderType = "SELL"

}
