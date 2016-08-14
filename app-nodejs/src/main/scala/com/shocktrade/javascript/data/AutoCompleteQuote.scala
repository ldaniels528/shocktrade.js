package com.shocktrade.javascript.data

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Auto-Completion Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AutoCompleteQuote extends js.Object {
  var symbol: String = js.native
  var name: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native
  var icon: js.UndefOr[String] = js.native
}

/**
  * Auto-Completion Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object AutoCompleteQuote {
  val Fields = js.Array("symbol", "name", "exchange", "assetType")

}