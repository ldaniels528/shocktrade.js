package com.shocktrade.services

import java.text.SimpleDateFormat
import java.util.Date

import com.github.ldaniels528.tabular.Tabular
import com.shocktrade.services.currency.{BitCoinMarketQuotesService, BitCoinRealTimeQuoteService}
import com.shocktrade.services.googlefinance.GoogleFinanceGetPricesService.GfGetPricesRequest
import com.shocktrade.services.googlefinance.{GoogleFinanceGetPricesService, GoogleFinanceTradingHistoryService}
import com.shocktrade.services.yahoofinance._
import org.joda.time.DateTime
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language._

/**
 * Services Test Suite
 * @author lawrence.daniels@gmail.com
 */
class ServicesTest() {
  private val tabular = new Tabular()

  implicit def duration2Long(f: FiniteDuration): Long = f.toMillis

  @Test
  def basicTechnicalAnalysis() {
    val quote = YFBasicTechnicalAnalysisService.getQuote("AAPL")
    tabular.transform(Seq(quote)) foreach System.out.println
  }

  @Test
  def bitCoinQuote() {
    val quotes = Seq(BitCoinRealTimeQuoteService.getQuote)
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def bitCoinMarketQuote() {
    val quotes = BitCoinMarketQuotesService.getQuotes()
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def bloombergProfile() {
    val quotes = Seq(BloombergProfileService.getProfile("BRK/A"))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def cikNumberTest() {
    val result = CikCompanySearchService.search("Ratos AB")
    tabular.transform(result) foreach System.out.println
  }

  @Test
  def gfTradingHistory() {
    val quotes = GoogleFinanceTradingHistoryService.getTradingHistory("GYST", DateTime.now().minusDays(5).toDate, new Date())
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def gfGetPrices() {
    val startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").parse("2015-05-21 06:30:00-0700").getTime / 1000L
    val req = GfGetPricesRequest("AAPL", startTime, intervalInSecs = 5, periodInDays = 1)
    val quotes = GoogleFinanceGetPricesService.getQuotes(req)
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def isharesHoldings() {
    val holdings = ISharesHoldingsDetailService.getHoldings("HDV")
    tabular.transform(holdings) foreach System.out.println
  }

  @Test
  def isharesHoldingsAsJSON() {
    import net.liftweb.json.Serialization.write
    import net.liftweb.json._
    implicit val formats = DefaultFormats

    val holdings = ISharesHoldingsDetailService.getHoldings("HDV")
    val jsonString = write(holdings)
    System.out.println(s"JSON = $jsonString")

    val holdings2 = parse(jsonString).extract[Array[ISharesHoldingsDetailService.Holding]]
    tabular.transform(holdings2) foreach System.out.println
  }

  @Test
  def nasdaqIntraDayQuotes() {
    import com.shocktrade.services.NASDAQIntraDayQuotesService.ET_0930_TO_0959
    val quotes = NASDAQIntraDayQuotesService.getQuotes("AMD", ET_0930_TO_0959)
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def nasdaqIntraDayQuotesByRange() {
    import com.shocktrade.services.NASDAQIntraDayQuotesService._
    import com.shocktrade.services.util.DateUtil._
    val quotes = NASDAQIntraDayQuotesService.getQuotesInRange("AMD", getTimeSlot(getTradeStartTime()), getTimeSlot(getTradeStopTime()))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def nasdaqStockQuote() {
    val svc = new NASDAQStockQuoteService()
    val quote = Await.result(svc.getQuote("FB"), 10 seconds)
    tabular.transform(Seq(quote)) foreach System.out.println
  }

  @Test
  def otcbbProfile() {
    val profile = Await.result(OTCBBProfileService.getProfile("AEGY"), 10 seconds)
    tabular.transform(Seq(profile)) foreach System.out.println
  }

  @Test
  def yfBizProfile() {
    val quotes = Seq(Await.result(YFBusinessProfileService.getProfile("AAPL"), 10 seconds))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfCurrencyQuote() {
    val quotes = Seq(Await.result(YFCurrencyQuoteService.getQuote("EUR=X"), 10 seconds))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfCSVStockQuote() {
    val params = YFStockQuoteService.getParams(
      "symbol", "exchange", "name", "lastTrade", "tradeDate", "tradeTime", "change", "changePct", "prevClose", "open", "close", "high", "low",
      "high52Week", "low52Week", "volume", "marketCap", "errorMessage", "ask", "askSize", "bid", "bidSize")

    val symbols = Seq("CMMCY.PK", "AAPL", "AMD", "AMZN", "GOOG", "INTC", "YHOO")
    val response = YFStockQuoteService.getCSVData(symbols, params)
    val lines = Await.result(response, 10 seconds).toSeq
    symbols zip lines foreach { case (symbol, line) => System.out.println(s"$symbol: $line") }
    System.out.println()

    val quotes = Await.result(YFStockQuoteService.getQuotes(symbols, params), 10 seconds)
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfEndOfDayQuotes() {
    val sdf = new SimpleDateFormat("MM-dd-yyyy")
    val quotes = YFEndOfDayQuoteService.getQuotes("AAPL", sdf.parse("05-01-2015"), sdf.parse("05-03-2015"))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfIntraDayQuotes() {
    val quotes = YFIntraDayQuotesService.getQuotes("AMD")
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfKeyStatistics() {
    val quotes = Seq(Await.result(YFKeyStatisticsService.getKeyStatistics("AAPL"), 10 seconds))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfRealtimeQuote() {
    val quotes = Seq(Await.result(YFRealtimeStockQuoteService.getQuote("BRK-A"), 10 seconds))
    tabular.transform(quotes) foreach System.out.println
  }

  @Test
  def yfSymbolSuggestion() {
    val results = Await.result(YFSymbolSuggestionService.search("Apple"), 10 seconds)
    tabular.transform(results) foreach System.out.println
  }

  @Test
  def yfOptionQuote() {
    val quotes = Await.result(YFOptionQuoteService.getQuotes("FB"), 10 seconds)
    tabular.transform(quotes) foreach System.out.println
  }

}
