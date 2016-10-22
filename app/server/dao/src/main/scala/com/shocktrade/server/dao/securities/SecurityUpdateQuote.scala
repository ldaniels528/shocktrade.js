package com.shocktrade.server.dao.securities

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a securities update quote
  */
@ScalaJSDefined
class SecurityUpdateQuote(val symbol: js.UndefOr[String],
                          val exchange: js.UndefOr[String],
                          val subExchange: js.UndefOr[String],
                          val lastTrade: js.UndefOr[Double],
                          val prevClose: js.UndefOr[Double],
                          val open: js.UndefOr[Double],
                          val close: js.UndefOr[Double],
                          val high: js.UndefOr[Double],
                          val low: js.UndefOr[Double],
                          val high52Week: js.UndefOr[Double],
                          val low52Week: js.UndefOr[Double],
                          val spread: js.UndefOr[Double],
                          val change: js.UndefOr[Double],
                          val changePct: js.UndefOr[Double],
                          val tradeDateTime: js.UndefOr[js.Date],
                          val tradeDate: js.UndefOr[String],
                          val tradeTime: js.UndefOr[String],
                          val volume: js.UndefOr[Double],
                          val marketCap: js.UndefOr[Double],
                          val target1Yr: js.UndefOr[Double],
                          val active: js.UndefOr[Boolean],
                          val errorMessage: js.UndefOr[String],
                          val yfCsvResponseTime: js.UndefOr[Double],
                          val yfCsvLastUpdated: js.UndefOr[js.Date]) extends js.Object