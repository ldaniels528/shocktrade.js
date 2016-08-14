package com.shocktrade.javascript.models.quote

import scala.scalajs.js

/**
  * Sector Information Quote
  * @author lawrence.daniels@gmail.com
  * @example {"symbol":"AAPL","sector":"Technology","industry":"Computer Manufacturing","subIndustry":"Communications Equipment","lastTrade":114.21}
  */
@js.native
trait SectorInfoQuote extends js.Object {
  var symbol: js.UndefOr[String] = js.native
  var sector: js.UndefOr[String] = js.native
  var industry: js.UndefOr[String] = js.native
  var subIndustry: js.UndefOr[String] = js.native
  var lastTrade: js.UndefOr[Double] = js.native
}

/**
  * Sector Information Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object SectorInfoQuote {
  val Fields = js.Array("symbol", "sector", "industry", "subIndustry", "lastTrade")

}