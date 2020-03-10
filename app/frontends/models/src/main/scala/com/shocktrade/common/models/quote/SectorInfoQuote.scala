package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
 * Sector Information Quote
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait SectorInfoQuote extends js.Object {
  // basic fields
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var lastTrade: js.UndefOr[Double] = js.native
  var active: js.UndefOr[Boolean] = js.native

  // sector/industry information
  var sector: js.UndefOr[String] = js.native
  var industry: js.UndefOr[String] = js.native
  var subIndustry: js.UndefOr[String] = js.native

}

/**
 * Sector Information Quote Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object SectorInfoQuote {
  val Fields = js.Array("symbol", "exchange", "name", "lastTrade", "active", "sector", "industry", "subIndustry")

}