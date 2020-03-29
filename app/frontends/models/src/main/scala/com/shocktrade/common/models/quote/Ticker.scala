package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
 * Represents a stock ticker
 * @param symbol        the given stock symbol (e.g. "AMD")
 * @param exchange      the exchange the stock trades upon (e.g. "NASDAQ")
 * @param lastTrade     the last sale amount
 * @param tradeDateTime the last sale date
 */
class Ticker(val symbol: js.UndefOr[String],
             val exchange: js.UndefOr[String],
             val lastTrade: js.UndefOr[Double],
             val tradeDateTime: js.UndefOr[js.Date]) extends js.Object
