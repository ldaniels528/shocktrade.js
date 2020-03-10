package com.shocktrade.common.models.quote

import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Order Quote
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OrderQuote extends js.Object {
  var symbol: js.UndefOr[String]
  var name: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var market: js.UndefOr[String]
  var lastTrade: js.UndefOr[Double]
  var tradeDateTime: js.UndefOr[js.Date]
  var tradeDate: js.UndefOr[js.Date]
  var tradeTime: js.UndefOr[String]
  var open: js.UndefOr[Double]
  var prevClose: js.UndefOr[Double]
  var high: js.UndefOr[Double]
  var low: js.UndefOr[Double]
  var change: js.UndefOr[Double]
  var changePct: js.UndefOr[Double]
  var spread: js.UndefOr[Double]
  var volume: js.UndefOr[Double]
  var high52Week: js.UndefOr[Double]
  var low52Week: js.UndefOr[Double]
  var active: js.UndefOr[Boolean]
}

/**
 * Order Quote Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderQuote {
  val Fields = List(
    "symbol", "name", "exchange", "market", "lastTrade", "tradeDateTime", "open", "prevClose", "high", "low",
    "change", "changePct", "spread", "volume", "high52Week", "low52Week", "active"
  )

  def apply(): OrderQuote = New[OrderQuote]

  def apply(symbol: String): OrderQuote = {
    val quote = New[OrderQuote]
    quote.symbol = symbol
    quote.active = true
    quote
  }

}