package com.shocktrade.javascript.models

import scala.scalajs.js

/**
 * Order Quote
 */
trait OrderQuote extends js.Object {
  var symbol: String = js.native
  var name: String = js.native
  var exchange: String = js.native
  var lastTrade: js.UndefOr[Double] = js.native
  var open: js.UndefOr[Double] = js.native
  var prevClose: js.UndefOr[Double] = js.native
  var high: js.UndefOr[Double] = js.native
  var low: js.UndefOr[Double] = js.native
  var high52Week: js.UndefOr[Double] = js.native
  var low52Week: js.UndefOr[Double] = js.native
  var volume: js.UndefOr[Long] = js.native
  var spread: js.UndefOr[Double] = js.native
}