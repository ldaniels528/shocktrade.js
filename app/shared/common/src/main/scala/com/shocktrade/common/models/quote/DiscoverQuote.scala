package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Discover Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DiscoverQuote extends CompleteQuote {
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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object DiscoverQuote {

  /**
    * Full Quote Constructors
    * @param quote the given [[CompleteQuote full quote]]
    */
  implicit class DiscoverQuoteConstructors(val quote: CompleteQuote) extends AnyVal {

    @inline
    def toDiscover = quote.asInstanceOf[DiscoverQuote]

  }

}