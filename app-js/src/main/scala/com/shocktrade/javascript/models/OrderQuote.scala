package com.shocktrade.javascript.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * Order Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OrderQuote extends js.Object {
  var symbol: UndefOr[String]
  var name: UndefOr[String]
  var exchange: UndefOr[String]
  var lastTrade: UndefOr[Double]
  var open: UndefOr[Double]
  var prevClose: UndefOr[Double]
  var high: UndefOr[Double]
  var low: UndefOr[Double]
  var high52Week: UndefOr[Double]
  var low52Week: UndefOr[Double]
  var volume: UndefOr[Long]
  var spread: UndefOr[Double]
  var active: UndefOr[Boolean]
}

/**
  * Order Quote Singleton
  */
object OrderQuote {

  def apply(symbol: UndefOr[String] = js.undefined,
            active: Boolean = true) = {
    val quote = New[OrderQuote]
    quote.symbol = symbol
    quote.active = active
    quote
  }

}