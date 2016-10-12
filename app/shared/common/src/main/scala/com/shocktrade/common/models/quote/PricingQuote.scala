package com.shocktrade.common.models.quote

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Pricing Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class PricingQuote(val symbol: String,
                   val lastTrade: js.UndefOr[Double],
                   val tradeDateTime: js.UndefOr[js.Date]) extends js.Object

/**
  * Pricing Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PricingQuote {
  val Fields = List("symbol", "lastTrade")

}