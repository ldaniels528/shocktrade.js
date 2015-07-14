package com.shocktrade.javascript.models

import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
 * Full Quote
 */
trait FullQuote extends OrderQuote {
  var legalType: UndefOr[String] = js.native
  var products: UndefOr[js.Array[js.Object]] = js.native
}

/**
 * Full Quote Singleton
 */
object FullQuote {

  def apply(symbol: UndefOr[String] = js.undefined,
            active: Boolean = true) = {
    val quote = makeNew[FullQuote]
    quote.symbol = symbol
    quote.active = active
    quote
  }

}