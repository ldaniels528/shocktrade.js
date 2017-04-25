package com.shocktrade.server.dao.securities

import scala.scalajs.js

/**
  * Securities Snapshot Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SnapshotQuote(val symbol: js.UndefOr[String],
                    val exchange: js.UndefOr[String],
                    val subExchange: js.UndefOr[String],
                    val lastTrade: js.UndefOr[Double],
                    val tradeDateTime: js.UndefOr[js.Date],
                    val tradeDate: js.UndefOr[String],
                    val tradeTime: js.UndefOr[String],
                    val volume: js.UndefOr[Double]) extends js.Object