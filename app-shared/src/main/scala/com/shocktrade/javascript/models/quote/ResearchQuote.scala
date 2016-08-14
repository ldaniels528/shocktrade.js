package com.shocktrade.javascript.models.quote

import scala.scalajs.js

/**
  * Research Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ResearchQuote extends js.Object {
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var market: js.UndefOr[String] = js.native
}

/**
  * ResearchQuote Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object ResearchQuote {
  val Fields = js.Array("symbol", "name", "exchange", "market", "lastTrade", "open", "close", "prevClose", "high", "low", "changePct", "spread", "volume")

}