package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Exposure Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ExposureQuote extends js.Object {
  var symbol: String = js.native
  var exchange: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var active: js.UndefOr[Boolean] = js.native

  // basics
  var lastTrade: js.UndefOr[Double] = js.native
  var tradeDateTime: js.UndefOr[js.Date] = js.native
  var changePct: js.UndefOr[Double] = js.native
  var prevClose: js.UndefOr[Double] = js.native
  var open: js.UndefOr[Double] = js.native
  var close: js.UndefOr[Double] = js.native
  var low: js.UndefOr[Double] = js.native
  var high: js.UndefOr[Double] = js.native
  var spread: js.UndefOr[Double] = js.native
  var low52Week: js.UndefOr[Double] = js.native
  var high52Week: js.UndefOr[Double] = js.native
  var volume: js.UndefOr[Long] = js.native

  // classification
  var sector: js.UndefOr[String] = js.native
  var industry: js.UndefOr[String] = js.native

}

/**
  * Exposure Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ExposureQuote {

  val Fields = List(
    "symbol", "exchange", "name", "active",
    "lastTrade", "tradeDateTime", "changePct", "prevClose",
    "open", "close", "low", "high", "spread", "low52Week", "high52Week", "volume", "sector", "industry"
  )

}