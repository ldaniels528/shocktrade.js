package com.shocktrade.common.dao.quotes

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Securities Snapshot Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class SnapshotQuote(val symbol: js.UndefOr[String],
                    val exchange: js.UndefOr[String],
                    val lastTrade: js.UndefOr[Double],
                    val tradeDateTime: js.UndefOr[js.Date],
                    val tradeTime: js.UndefOr[String],
                    val volume: js.UndefOr[Double]) extends js.Object