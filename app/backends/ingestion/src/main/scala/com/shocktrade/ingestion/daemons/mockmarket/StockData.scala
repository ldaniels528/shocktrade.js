package com.shocktrade.ingestion.daemons.mockmarket

import io.scalajs.util.JsUnderOrHelper._
import scala.scalajs.js

/**
 * Stock Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class StockData(val symbol: js.UndefOr[String],
                val exchange: js.UndefOr[String],
                val name: js.UndefOr[String],
                val cikNumber: js.UndefOr[String],
                val sector: js.UndefOr[String],
                val industry: js.UndefOr[String],
                val lastTrade: js.UndefOr[Double],
                val tradeDateTime: js.UndefOr[js.Date],
                val prevClose: js.UndefOr[Double],
                val open: js.UndefOr[Double],
                val close: js.UndefOr[Double],
                val high: js.UndefOr[Double],
                val low: js.UndefOr[Double],
                val spread: js.UndefOr[Double],
                val change: js.UndefOr[Double],
                val changePct: js.UndefOr[Double],
                val beta: js.UndefOr[Double],
                val avgVolume10Day: js.UndefOr[Double],
                val volume: js.UndefOr[Double]) extends js.Object

/**
 * Stock Data Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StockData {

  final implicit class StockDataCaseOperations(val stock: StockData) extends AnyVal {

    def copy(symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             cikNumber: js.UndefOr[String] = js.undefined,
             sector: js.UndefOr[String] = js.undefined,
             industry: js.UndefOr[String] = js.undefined,
             lastTrade: js.UndefOr[Double] = js.undefined,
             tradeDateTime: js.UndefOr[js.Date] = js.undefined,
             prevClose: js.UndefOr[Double] = js.undefined,
             open: js.UndefOr[Double] = js.undefined,
             close: js.UndefOr[Double] = js.undefined,
             high: js.UndefOr[Double] = js.undefined,
             low: js.UndefOr[Double] = js.undefined,
             spread: js.UndefOr[Double] = js.undefined,
             change: js.UndefOr[Double] = js.undefined,
             changePct: js.UndefOr[Double] = js.undefined,
             beta: js.UndefOr[Double] = js.undefined,
             avgVolume10Day: js.UndefOr[Double] = js.undefined,
             volume: js.UndefOr[Double] = js.undefined): StockData = {
      new StockData(
        symbol = symbol ?? stock.symbol,
        exchange = exchange ?? stock.exchange,
        name = name ?? stock.name,
        cikNumber = cikNumber ?? stock.cikNumber,
        sector = sector ?? stock.sector,
        industry = industry ?? stock.industry,
        lastTrade = lastTrade ?? stock.lastTrade,
        tradeDateTime = tradeDateTime ?? stock.tradeDateTime,
        prevClose = prevClose ?? stock.prevClose,
        open = open ?? stock.open,
        close = close ?? stock.close,
        high = high ?? stock.high,
        low = low ?? stock.low,
        spread = spread ?? stock.spread,
        change = change ?? stock.change,
        changePct = changePct ?? stock.changePct,
        beta = beta ?? stock.beta,
        avgVolume10Day = avgVolume10Day ?? stock.avgVolume10Day,
        volume = volume ?? stock.volume
      )
    }
  }

}
