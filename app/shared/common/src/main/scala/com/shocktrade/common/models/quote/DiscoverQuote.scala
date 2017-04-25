package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
  * Discover Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
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
    def toDiscover: DiscoverQuote = {
      val q = quote.asInstanceOf[DiscoverQuote]
      q.getAdvisory foreach { advisory =>
        q.advisory = advisory.description
        q.advisoryType = advisory.`type`
      }
      q.riskLevel = q.getRiskLevel
      q
    }

  }

}