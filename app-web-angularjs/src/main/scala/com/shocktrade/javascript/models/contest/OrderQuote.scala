package com.shocktrade.javascript.models.contest

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Order Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OrderQuote extends js.Object {
  var symbol: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var lastTrade: js.UndefOr[Double] = js.native
  var open: js.UndefOr[Double] = js.native
  var prevClose: js.UndefOr[Double] = js.native
  var high: js.UndefOr[Double] = js.native
  var low: js.UndefOr[Double] = js.native
  var high52Week: js.UndefOr[Double] = js.native
  var low52Week: js.UndefOr[Double] = js.native
  var volume: js.UndefOr[Long] = js.native
  var spread: js.UndefOr[Double] = js.native
  var active: js.UndefOr[Boolean] = js.native
}

/**
  * Order Quote Singleton
  */
object OrderQuote {

  def apply(symbol: js.UndefOr[String] = js.undefined,
            active: Boolean = true) = {
    val quote = New[OrderQuote]
    quote.symbol = symbol
    quote.active = active
    quote
  }

}