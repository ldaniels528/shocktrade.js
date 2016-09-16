package com.shocktrade.stockguru.news

import scala.scalajs.js

/**
  * News Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NewsQuote extends js.Object {
  var name: js.UndefOr[String] = js.native
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var sector: js.UndefOr[String] = js.native
  var industry: js.UndefOr[String] = js.native
  var changePct: js.UndefOr[Double] = js.native
}
