package com.shocktrade.server.dao.contest

import scala.scalajs.js

/**
  * Represents a claimed transition, which will eventually result in a new position
  * @param symbol   the symbol (.e.g "AAPL")
  * @param exchange the exchange
  * @param price    the price
  * @param quantity the quantity
  * @param asOfTime the effective time
  */
class Claim(val symbol: String,
            val exchange: String,
            val price: Double,
            val quantity: Double,
            val asOfTime: js.Date) extends js.Object
