package com.shocktrade.common.models.quote

import io.scalajs.util.JsUnderOrHelper._
import scala.scalajs.js

/**
 * Represents a Research Quote
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ResearchQuote(val symbol: js.UndefOr[String] = js.undefined,
                    val name: js.UndefOr[String] = js.undefined,
                    val exchange: js.UndefOr[String] = js.undefined,
                    val market: js.UndefOr[String] = js.undefined,
                    val lastTrade: js.UndefOr[Double] = js.undefined,
                    val open: js.UndefOr[Double] = js.undefined,
                    val close: js.UndefOr[Double] = js.undefined,
                    val prevClose: js.UndefOr[Double] = js.undefined,
                    val high: js.UndefOr[Double] = js.undefined,
                    val low: js.UndefOr[Double] = js.undefined,
                    val change: js.UndefOr[Double] = js.undefined,
                    val changePct: js.UndefOr[Double] = js.undefined,
                    val spread: js.UndefOr[Double] = js.undefined,
                    val volume: js.UndefOr[Double] = js.undefined,
                    val avgVolume10Day: js.UndefOr[Double] = js.undefined,
                    val beta: js.UndefOr[Double] = js.undefined,
                    val active: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
 * Research Quote Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResearchQuote {
  val Fields = List(
    "symbol", "name", "exchange", "market", "lastTrade", "open", "close", "prevClose", "high", "low",
    "change", "changePct", "spread", "volume", "avgVolume10Day", "beta", "active"
  )

  final implicit class ResearchQuoteEnriched(val quote: ResearchQuote) extends AnyVal {

    def copy(symbol: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             market: js.UndefOr[String] = js.undefined,
             lastTrade: js.UndefOr[Double] = js.undefined,
             open: js.UndefOr[Double] = js.undefined,
             close: js.UndefOr[Double] = js.undefined,
             prevClose: js.UndefOr[Double] = js.undefined,
             high: js.UndefOr[Double] = js.undefined,
             low: js.UndefOr[Double] = js.undefined,
             change: js.UndefOr[Double] = js.undefined,
             changePct: js.UndefOr[Double] = js.undefined,
             spread: js.UndefOr[Double] = js.undefined,
             volume: js.UndefOr[Double] = js.undefined,
             avgVolume10Day: js.UndefOr[Double] = js.undefined,
             beta: js.UndefOr[Double] = js.undefined,
             active: js.UndefOr[Boolean] = js.undefined): ResearchQuote = {
      new ResearchQuote(
        symbol = symbol ?? quote.symbol,
        name = name ?? quote.name,
        exchange = exchange ?? quote.exchange,
        market = market ?? quote.market,
        lastTrade = lastTrade ?? quote.lastTrade,
        open = open ?? quote.open,
        close = close ?? quote.close,
        prevClose = prevClose ?? quote.prevClose,
        high = high ?? quote.high,
        low = low ?? quote.low,
        change = change ?? quote.change,
        changePct = changePct ?? quote.changePct,
        spread = spread ?? quote.spread,
        volume = volume ?? quote.volume,
        avgVolume10Day = avgVolume10Day ?? quote.avgVolume10Day,
        beta = beta ?? quote.beta,
        active = active ?? quote.active)
    }

  }

}