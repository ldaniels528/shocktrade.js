package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Historical Quote Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait HistoricalQuote extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var symbol: String = js.native
}
