package com.shocktrade.javascript.models

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * Order Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OrderQuote extends js.Object {
  var symbol: UndefOr[String] = js.native
  var name: UndefOr[String] = js.native
  var exchange: UndefOr[String] = js.native
  var lastTrade: UndefOr[Double] = js.native
  var open: UndefOr[Double] = js.native
  var prevClose: UndefOr[Double] = js.native
  var high: UndefOr[Double] = js.native
  var low: UndefOr[Double] = js.native
  var high52Week: UndefOr[Double] = js.native
  var low52Week: UndefOr[Double] = js.native
  var volume: UndefOr[Long] = js.native
  var spread: UndefOr[Double] = js.native
  var active: UndefOr[Boolean] = js.native
}

/**
  * Order Quote Singleton
  */
object OrderQuote {

  def apply(symbol: UndefOr[String] = js.undefined,
            active: Boolean = true) = {
    val quote = makeNew[OrderQuote]
    quote.symbol = symbol
    quote.active = active
    quote
  }

}