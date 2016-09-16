package com.shocktrade.common.models.quote

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Research Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ResearchQuote(var symbol: js.UndefOr[String] = js.undefined,
                    var name: js.UndefOr[String] = js.undefined,
                    var exchange: js.UndefOr[String] = js.undefined,
                    var market: js.UndefOr[String] = js.undefined,
                    var lastTrade: js.UndefOr[Double] = js.undefined,
                    var open: js.UndefOr[Double] = js.undefined,
                    var close: js.UndefOr[Double] = js.undefined,
                    var prevClose: js.UndefOr[Double] = js.undefined,
                    var high: js.UndefOr[Double] = js.undefined,
                    var low: js.UndefOr[Double] = js.undefined,
                    var changePct: js.UndefOr[Double] = js.undefined,
                    var spread: js.UndefOr[Double] = js.undefined,
                    var volume: js.UndefOr[Double] = js.undefined,
                    var avgVolume10Day: js.UndefOr[Double] = js.undefined,
                    var beta: js.UndefOr[Double] = js.undefined,
                    var active: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
  * Research Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ResearchQuote {

  val Fields = List(
    "symbol", "name", "exchange", "market", "lastTrade", "open", "close", "prevClose", "high", "low",
    "changePct", "spread", "volume", "avgVolume10Day", "beta", "active"
  )

  /**
    * Basic Quote Extensions
    * @param quote the given [[ResearchQuote basic quote]]
    */
  implicit class ResearchQuoteEnrichment(val quote: ResearchQuote) extends AnyVal {

    /**
      * Returns the OTC advisory for the given symbol
      * @return the OTC advisory for the given symbol
      */
    @inline
    def getAdvisory = quote.symbol.flatMap(Advisory(_, quote.exchange))

    @inline
    def getRiskLevel = {
      quote.beta map {
        case b if b >= 0 && b <= 1.25 => "Low"
        case b if b > 1.25 && b <= 1.9 => "Medium"
        case _ => "High"
      } getOrElse "Unknown"
    }

  }

}