package com.shocktrade.common.models.quote

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Discover Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait DiscoverQuote extends CompleteQuote {
  // standard codes
  var advisory: js.UndefOr[String]
  var advisoryType: js.UndefOr[String]
  var naicsDescription: js.UndefOr[String]
  var sicDescription: js.UndefOr[String]

  // risk
  var riskLevel: js.UndefOr[String]

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