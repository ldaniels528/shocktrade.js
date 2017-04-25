package com.shocktrade.server.facade

import scala.scalajs.js

/**
  * Pricing Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PricingQuote(val symbol: String,
                   val lastTrade: js.UndefOr[Double],
                   val tradeDateTime: js.UndefOr[js.Date]) extends js.Object

/**
  * Pricing Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PricingQuote {
  val Fields = List("symbol", "lastTrade", "tradeDateTime")

}