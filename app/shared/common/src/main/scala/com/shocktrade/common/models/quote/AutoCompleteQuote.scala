package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Auto-Completion Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait AutoCompleteQuote extends js.Object {
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native
  var icon: js.UndefOr[String] = js.native
}

/**
  * Auto-Completion Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object AutoCompleteQuote {

  val Fields = List("symbol", "name", "exchange", "assetType")

}