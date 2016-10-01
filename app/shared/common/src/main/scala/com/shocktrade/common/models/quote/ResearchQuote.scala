package com.shocktrade.common.models.quote

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Research Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait ResearchQuote extends js.Object {
  var symbol: js.UndefOr[String]
  var name: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var market: js.UndefOr[String]
  var lastTrade: js.UndefOr[Double]
  var open: js.UndefOr[Double]
  var close: js.UndefOr[Double]
  var prevClose: js.UndefOr[Double]
  var high: js.UndefOr[Double]
  var low: js.UndefOr[Double]
  var change: js.UndefOr[Double]
  var changePct: js.UndefOr[Double]
  var spread: js.UndefOr[Double]
  var volume: js.UndefOr[Double]
  var avgVolume10Day: js.UndefOr[Double]
  var beta: js.UndefOr[Double]
  var active: js.UndefOr[Boolean]
}

/**
  * Research Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ResearchQuote {

  val Fields = List(
    "symbol", "name", "exchange", "market", "lastTrade", "open", "close", "prevClose", "high", "low",
    "change", "changePct", "spread", "volume", "avgVolume10Day", "beta", "active"
  )

  def apply() = New[ResearchQuote]

  def apply(symbol: String) = {
    val quote = New[ResearchQuote]
    quote.symbol = symbol
    quote.active = true
    quote
  }

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