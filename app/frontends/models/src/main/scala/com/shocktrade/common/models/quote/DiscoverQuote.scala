package com.shocktrade.common.models.quote

import scala.scalajs.js
import scala.scalajs.js.UndefOr

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

  /**
   * Basic Quote Extensions
   * @param quote the given [[CompleteQuote basic quote]]
   */
  implicit class ResearchQuoteEnrichment(val quote: CompleteQuote) extends AnyVal {

    /**
     * Returns the OTC advisory for the given symbol
     * @return the OTC advisory for the given symbol
     */
    @inline
    def getAdvisory: UndefOr[Advisory] = quote.symbol.flatMap(Advisory(_, quote.exchange))

    @inline
    def getRiskLevel: String = {
      quote.beta map {
        case b if b >= 0 && b <= 1.25 => "Low"
        case b if b > 1.25 && b <= 1.9 => "Medium"
        case _ => "High"
      } getOrElse "Unknown"
    }

  }

}