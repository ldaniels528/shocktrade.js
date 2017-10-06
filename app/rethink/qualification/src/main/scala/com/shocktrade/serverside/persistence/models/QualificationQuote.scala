package com.shocktrade.serverside.persistence.models

import scala.scalajs.js

/**
  * Represents a quote that is used during the Qualification process
  * @param symbol        the given symbol (e.g. "AAPL")
  * @param lastSale      the last sales price (e.g. 165.0)
  * @param tradeDateTime the last trading [[js.Date date/time]]
  */
class QualificationQuote(val symbol: String,
                         val exchange: String,
                         val lastSale: Double,
                         val tradeDateTime: js.Date) extends js.Object