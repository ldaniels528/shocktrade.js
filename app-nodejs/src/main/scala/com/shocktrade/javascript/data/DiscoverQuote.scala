package com.shocktrade.javascript.data

import scala.scalajs.js

/**
  * Discover Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait DiscoverQuote extends FullQuote {
  // standard codes
  var advisory: js.UndefOr[String] = js.native
  var advisoryType: js.UndefOr[String] = js.native
  var naicsDescription: js.UndefOr[String] = js.native
  var sicDescription: js.UndefOr[String] = js.native

  // risk
  var riskLevel: js.UndefOr[String] = js.native

}

/**
  * Discover Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object DiscoverQuote {

  /**
    * Full Quote Constructors
    * @param quote the given [[FullQuote full quote]]
    */
  implicit class DiscoverQuoteConstructors(val quote: FullQuote) extends AnyVal {

    @inline
    def toDiscover = quote.asInstanceOf[DiscoverQuote]

  }

}