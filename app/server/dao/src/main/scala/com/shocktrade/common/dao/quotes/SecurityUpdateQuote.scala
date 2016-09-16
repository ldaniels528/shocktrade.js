package com.shocktrade.common.dao.quotes

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a securities update quote
  */
@ScalaJSDefined
class SecurityUpdateQuote(val symbol: js.UndefOr[String],
                          val exchange: js.UndefOr[String],
                          val lastTrade: js.UndefOr[Double],
                          val open: js.UndefOr[Double],
                          val close: js.UndefOr[Double],
                          val tradeDateTime: js.UndefOr[js.Date],
                          val tradeDate: js.UndefOr[js.Date],
                          val tradeTime: js.UndefOr[String],
                          val volume: js.UndefOr[Double],
                          val errorMessage: js.UndefOr[String],
                          val yfCsvResponseTime: js.UndefOr[Double],
                          val yfCsvLastUpdated: js.UndefOr[js.Date]) extends js.Object