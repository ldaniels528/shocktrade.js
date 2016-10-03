package com.shocktrade.services.yahoo

import com.shocktrade.services.ScriptParser
import com.shocktrade.services.yahoo.YahooFinanceKeyStatisticsService._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.request.Request
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Yahoo Finance! Key Statistics Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceKeyStatisticsService()(implicit require: NodeRequire) {
  private val scriptParser = new ScriptParser[YFKeyStatistics]()
  private val request = Request()

  /**
    * Attempts to retrieve the statistics for the given symbol
    * @param symbol the given symbol
    * @return the promise of the option of a [[YFKeyStatistics key statistics]] object
    */
  def apply(symbol: String)(implicit ec: ExecutionContext) = {
    for {
      (response, html) <- request.getFuture(s"https://finance.yahoo.com/quote/$symbol/key-statistics")
      keyStats_? <- scriptParser.parse(html, anchor = "\"QuoteSummaryStore\":")
    } yield keyStats_?
  }

}

/**
  * Yahoo Finance! Statistics Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceKeyStatisticsService {

  /**
    * Represents the Yahoo! Finance Quote Summary Store object
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @js.native
  trait YFKeyStatistics extends js.Object {
    var symbol: js.UndefOr[String] = js.native
    var price: js.UndefOr[YFPriceType] = js.native
    var quoteType: js.UndefOr[YFQuoteType] = js.native
    var summaryDetail: js.UndefOr[YFSummaryDetail] = js.native
  }

  @js.native
  trait YFPriceType extends js.Object {
    var averageDailyVolume10Day: js.UndefOr[YFQuantityType] = js.native
    var averageDailyVolume3Month: js.UndefOr[YFQuantityType] = js.native
    var currency: js.UndefOr[String] = js.native
    var currencySymbol: js.UndefOr[String] = js.native
    var exchange: js.UndefOr[String] = js.native
    var exchangeName: js.UndefOr[String] = js.native
    var longName: js.UndefOr[String] = js.native
    var marketState: js.UndefOr[String] = js.native
    var maxAge: js.UndefOr[Double] = js.native
    var openInterest: js.UndefOr[YFQuantityType] = js.native
    var postMarketChange: js.UndefOr[YFQuantityType] = js.native
    var postMarketChangePercent: js.UndefOr[YFQuantityType] = js.native
    var postMarketPrice: js.UndefOr[YFQuantityType] = js.native
    var postMarketSource: js.UndefOr[String] = js.native
    var postMarketTime: js.UndefOr[Double] = js.native
    var preMarketChange: js.UndefOr[YFQuantityType] = js.native
    var preMarketPrice: js.UndefOr[YFQuantityType] = js.native
    var preMarketSource: js.UndefOr[String] = js.native
    var quoteType: js.UndefOr[String] = js.native
    var regularMarketChange: js.UndefOr[YFQuantityType] = js.native
    var regularMarketChangePercent: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayHigh: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayLow: js.UndefOr[YFQuantityType] = js.native
    var regularMarketOpen: js.UndefOr[YFQuantityType] = js.native
    var regularMarketPreviousClose: js.UndefOr[YFQuantityType] = js.native
    var regularMarketSource: js.UndefOr[String] = js.native
    var regularMarketTime: js.UndefOr[Double] = js.native
    var regularMarketVolume: js.UndefOr[YFQuantityType] = js.native
    var shortName: js.UndefOr[String] = js.native
    var strikePrice: js.UndefOr[YFQuantityType] = js.native
    var symbol: js.UndefOr[String] = js.native
    var underlyingSymbol: js.UndefOr[String] = js.native
  }

  @js.native
  trait YFQuoteType extends js.Object {
    var uuid: js.UndefOr[String] = js.native
    var symbol: js.UndefOr[String] = js.native
    var exchange: js.UndefOr[String] = js.native
    var shortName: js.UndefOr[String] = js.native
    var longName: js.UndefOr[String] = js.native
    var headSymbol: js.UndefOr[String] = js.native
    var underlyingSymbol: js.UndefOr[String] = js.native
    var underlyingExchangeSymbol: js.UndefOr[String] = js.native
    var market: js.UndefOr[String] = js.native
    var quoteType: js.UndefOr[String] = js.native
    var messageBoardId: js.UndefOr[String] = js.native
  }

  @js.native
  trait YFSummaryDetail extends js.Object {
    var ask: js.UndefOr[YFQuantityType] = js.native
    var askSize: js.UndefOr[YFQuantityType] = js.native
    var averageDailyVolume10Day: js.UndefOr[YFQuantityType] = js.native
    var averageVolume: js.UndefOr[YFQuantityType] = js.native
    var averageVolume10days: js.UndefOr[YFQuantityType] = js.native
    var beta: js.UndefOr[YFQuantityType] = js.native
    var bid: js.UndefOr[YFQuantityType] = js.native
    var bidSize: js.UndefOr[YFQuantityType] = js.native
    var dayHigh: js.UndefOr[YFQuantityType] = js.native
    var dayLow: js.UndefOr[YFQuantityType] = js.native
    var dividendRate: js.UndefOr[YFQuantityType] = js.native
    var dividendYield: js.UndefOr[YFQuantityType] = js.native
    var exDividendDate: js.UndefOr[YFQuantityType] = js.native
    var expireDate: js.UndefOr[YFQuantityType] = js.native
    var fiftyDayAverage: js.UndefOr[YFQuantityType] = js.native
    var fiftyTwoWeekHigh: js.UndefOr[YFQuantityType] = js.native
    var fiftyTwoWeekLow: js.UndefOr[YFQuantityType] = js.native
    var fiveYearAvgDividendYield: js.UndefOr[YFQuantityType] = js.native
    var forwardPE: js.UndefOr[YFQuantityType] = js.native
    var marketCap: js.UndefOr[YFQuantityType] = js.native
    var maxAge: js.UndefOr[Int] = js.native
    var navPrice: js.UndefOr[YFQuantityType] = js.native
    var openInterest: js.UndefOr[YFQuantityType] = js.native
    var previousClose: js.UndefOr[YFQuantityType] = js.native
    var priceToSalesTrailing12Months: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayLow: js.UndefOr[YFQuantityType] = js.native
    var regularMarketOpen: js.UndefOr[YFQuantityType] = js.native
    var regularMarketPreviousClose: js.UndefOr[YFQuantityType] = js.native
    var regularMarketVolume: js.UndefOr[YFQuantityType] = js.native
    var strikePrice: js.UndefOr[YFQuantityType] = js.native
    var totalAssets: js.UndefOr[YFQuantityType] = js.native
    var trailingAnnualDividendRate: js.UndefOr[YFQuantityType] = js.native
    var trailingAnnualDividendYield: js.UndefOr[YFQuantityType] = js.native
    var trailingPE: js.UndefOr[YFQuantityType] = js.native
    var twoHundredDayAverage: js.UndefOr[YFQuantityType] = js.native
    var volume: js.UndefOr[YFQuantityType] = js.native
    var `yield`: js.UndefOr[YFQuantityType] = js.native
    var ytdReturn: js.UndefOr[YFQuantityType] = js.native
  }

  @js.native
  trait YFQuantityType extends js.Object {
    var raw: js.UndefOr[Double] = js.native
    var fmt: js.UndefOr[String] = js.native
    var longFmt: js.UndefOr[String] = js.native
  }

}