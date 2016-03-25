package com.shocktrade.javascript.models

import scala.scalajs.js

/**
  * Historical Quote Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait HistoricalQuote extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var symbol: String
}
