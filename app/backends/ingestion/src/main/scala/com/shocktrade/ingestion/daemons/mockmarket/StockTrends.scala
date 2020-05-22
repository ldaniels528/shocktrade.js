package com.shocktrade.ingestion.daemons.mockmarket

import com.shocktrade.server.common.LoggerFactory
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.util.Random

/**
 * Stock Trends
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StockTrends {
  private val logger = LoggerFactory.getLogger(getClass)
  private val sectorTrends = js.Dictionary[SectorTrend]()
  private val tickerTrends = js.Dictionary[TickerTrend]()
  private var symbols: Seq[String] = Nil
  private val random = new Random()

  /**
   * Returns the new sale price for the given stock
   * @param stock the given [[StockData stock]]
   * @return the new sale price
   */
  def getNewSale(stock: StockData): js.UndefOr[Double] = {
    for {
      symbol <- stock.symbol
      lastTrade <- stock.lastTrade ?? stock.lowLimit

      sectorTrend = sectorTrends.getOrElseUpdate(stock.sector.orNull, new SectorTrend())
      tickerTrend = tickerTrends.getOrElseUpdate(symbol, new TickerTrend())
      _ = tickerTrend.ticker += sectorTrend.delta

      wave = Math.cos(tickerTrend.ticker + sectorTrend.offset) * (0.01 * lastTrade)
      highLimit <- stock.highLimit
      lowLimit <- stock.lowLimit
      newSale = Math.max(lastTrade, lowLimit) + wave
      newSaleBoxed = Math.min(highLimit, Math.max(lowLimit, newSale))

      _ = if (symbols.contains(symbol)) {
        logger.info(f"$symbol | low $lowLimit%.2f\t| high $highLimit%.2f\t| new $newSale%.2f ~ $newSaleBoxed%.2f\t| change ${stock.change.orZero}%.2f ~ ${stock.changePct.orZero}%.2f%% | wave = $wave%.4f, t = ${tickerTrend.ticker}%.4f | sector = ${stock.sector.orNull}")
      }
    } yield newSaleBoxed
  }

  /**
   * Sets the symbols that should be monitored
   * @param symbols the given symbols to watch
   */
  def watch(symbols: String*): Unit = this.symbols = symbols

  /**
   * Represents a sector trend
   * @param delta  the delta value
   * @param offset the offset value
   */
  class SectorTrend(var delta: Double = Math.PI / 28, var offset: Double = random.nextDouble() * Math.PI) extends js.Object

  /**
   * Represents a ticker trend
   * @param ticker the ticker value
   */
  class TickerTrend(var ticker: Double = random.nextDouble() * Math.PI) extends js.Object

}
