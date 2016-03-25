package com.shocktrade.services

import com.github.ldaniels528.tabular.Tabular
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import org.junit.Test

import scala.language.postfixOps

class YFStockQuoteParserTest() {
  val logger = org.slf4j.LoggerFactory.getLogger(getClass)
  val tabular = new Tabular()

  // http://finance.yahoo.com/d/quotes.csv?s=MSFT&f=l1e1a5b0h0a0g0v0p0b6s0d1t1o0q2

  @Test
  def test() {
    val params = "l1e1a5b0h0a0g0v0p0b6s0d1t1o0q2"
    val csvdata = """37.42,"N/A",4,400,37.41,37.47,37.42,37.217,2543590,37.29,5,600,"MSFT","12/31/2013","10:17am",37.33,37.42"""
    val quotes = Seq(YFStockQuoteService.parseQuote("AMD", params, csvdata, System.currentTimeMillis()))
    tabular.transform(quotes) foreach println
  }

  @Test
  def askBidSize() {
    import scala.concurrent.Await
    import scala.concurrent.ExecutionContext.Implicits._
    import scala.concurrent.duration._

    val symbol = "AAPL"
    val params2 = YFStockQuoteService.getParams(
      "symbol", "lastTrade", "tradeDate", "tradeTime",
      "change", "changePct", "prevClose", "open", "close", "high", "low", "volume", "marketCap", "errorMessage", "ask", "bid", "askSize", "bidSize")
    val params = YFStockQuoteService.getParams("askSize")

    val csvdata = Await.result(YFStockQuoteService.getCSVData(Seq(symbol), params), 10 seconds) mkString ""
    val quotes = Seq(YFStockQuoteService.parseQuote(symbol, params, csvdata, System.currentTimeMillis()))

    // capture the start time
    println(s"params=[$params] csv=[$csvdata]")

    tabular.transform(quotes) foreach println
  }

}