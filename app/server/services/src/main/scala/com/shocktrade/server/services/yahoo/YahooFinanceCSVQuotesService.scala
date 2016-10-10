package com.shocktrade.server.services.yahoo

import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.request._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Yahoo Finance! CSV Quotes Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceCSVQuotesService()(implicit require: NodeRequire) {
  private val parser = new YahooFinanceCSVQuotesParser()
  private val request = Request()

  /**
    * Performs the service call and returns a single object read from the service.
    * @param symbols the given stock symbols (e.g., "AAPL", "AMD", "INTC")
    * @param params  the given stock fields (e.g., "soxq2")
    * @return the [[YFCSVQuote quote]]
    */
  def getQuotes(params: String, symbols: String*)(implicit ec: ExecutionContext) = {
    val startTime = js.Date.now()
    val symbolList = symbols mkString "+"
    request.getFuture(s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params") map { case (response, data) =>
      val lines = data.split("[\n]")
      (symbols zip lines) map { case (symbol, line) => parser.parseQuote(symbol, params, line, startTime) }
    }
  }

  /**
    * Returns all supported parameter codes
    * @return all supported parameter codes
    */
  def getAllParams: String = FIELD_CODE_TO_MAPPING.keys.mkString

  /**
    * Returns the parameter codes required to retrieve values for the given fields
    * @return the parameter codes required to retrieve values for the given fields
    */
  def getParams(fields: String*): String = {
    (fields flatMap FIELD_CODE_TO_MAPPING.get).map(c => if (c.endsWith("0")) c.dropRight(1) else c).mkString
  }

}

/**
  * Yahoo Finance! CSV Quotes Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceCSVQuotesService {

  /**
    * Represents a Yahoo! Finance Stock Quote (Web Service Object)
    * @see http://www.gummy-stuff.org/Yahoo-data.htm
    * @see http://www.codeproject.com/Articles/37550/Stock-quote-and-chart-from-Yahoo-in-C
    * @see http://people.stern.nyu.edu/adamodar/New_Home_Page/data.html
    * @see http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
    * @see http://code.google.com/p/yahoo-finance-managed/wiki/CSVAPI
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class YFCSVQuote(val symbol: String,
                   val ask: js.UndefOr[Double], // a0 - Ask
                   val avgVol: js.UndefOr[Double], // a2 - Average Daily Volume
                   val askSize: js.UndefOr[Int], // a5 - Ask Size
                   val bid: js.UndefOr[Double], // b0 - Bid
                   // b1 - ???? Ask/Bid?
                   val askRealTime: js.UndefOr[Double], // b2 - Ask (Real-time)
                   val bidRealTime: js.UndefOr[Double], // b3 - Bid (Real-time)
                   val bookValuePerShare: js.UndefOr[Double], // b4 - Book Value Per Share
                   val bidSize: js.UndefOr[Int], // b6 - Bid Size
                   // c0 - Change & Percent Change
                   val change: js.UndefOr[Double], // c1 - Change
                   val commission: js.UndefOr[Double], // c3 - Commission
                   val currencyCode: js.UndefOr[String], // c4 - Currency Code
                   val changeRealTime: js.UndefOr[Double], // c6 - Change (Real-time)
                   val changeAfterHours: js.UndefOr[Double], // c8 - After Hours Change (Real-time)
                   val divShare: js.UndefOr[Double], // d0 - Dividend/Share (Trailing Annual)
                   val tradeDate: js.UndefOr[String], // d1 - Last Trade Date
                   val tradeDateTime: js.UndefOr[js.Date],
                   // d2 - Trade Date? (-)
                   // d3 - Last Trade Time? ("Feb  3", "10:56am")
                   val eps: js.UndefOr[Double], // e0 - Earnings/Share (Diluted)
                   val errorMessage: js.UndefOr[String], // e1 - Error Indication (returned for symbol changed / invalid)
                   val epsEstCurrentYear: js.UndefOr[Double], // e7 - EPS Estimate Current Year
                   val epsEstNextYear: js.UndefOr[Double], // e8 - EPS Estimate Next Year
                   val epsEstNextQtr: js.UndefOr[Double], // e9 - EPS Estimate Next Quarter
                   val floatShares: js.UndefOr[Double], // f6 - Float Shares
                   val low: js.UndefOr[Double], // g0 - Day's Low
                   val holdingsGainPct: js.UndefOr[Double], // g1 - Holdings Gain Percent
                   val annualizedGain: js.UndefOr[Double], // g3 - Annualized Gain
                   val holdingsGain: js.UndefOr[Double], // g4 - Holdings Gain
                   val holdingsGainPctRealTime: js.UndefOr[Double], // g5 - Holdings Gain Percent (Real-time)
                   val holdingsGainRealTime: js.UndefOr[Double], // g6 - Holdings Gain (Real-time)
                   val high: js.UndefOr[Double], // h0 - Day's High
                   val moreInfo: js.UndefOr[String], // i0 - More Info
                   val orderBookRealTime: js.UndefOr[Double], // i5 - Order Book (Real-time)
                   val low52Week: js.UndefOr[Double], // j0 - 52-week Low
                   val marketCap: js.UndefOr[Double], // j1 - Market Capitalization
                   val sharesOutstanding: js.UndefOr[Double], // j2 - Shares Outstanding
                   val marketCapRealTime: js.UndefOr[Double], // j3 - Market Capitalization (Real-time)
                   val EBITDA: js.UndefOr[Double], // j4 - EBITDA
                   val change52WeekLow: js.UndefOr[Double], // j5 - Change From 52-week Low
                   val changePct52WeekLow: js.UndefOr[Double], // j6 - Percent Change From 52-week Low
                   val high52Week: js.UndefOr[Double], // k0 - 52-week High
                   // k1 - Last Trade With Time (Real-time)
                   val changePctRealTime: js.UndefOr[Double], // k2 - Change Percent (Real-time)
                   val lastTradeSize: js.UndefOr[Int], // k3 - Last Trade Size
                   val change52WeekHigh: js.UndefOr[Double], // k4 - Change From 52-week High
                   val changePct52WeekHigh: js.UndefOr[Double], // k5 - Percent Change From 52-week High
                   // l0 - Last Trade (With Time)
                   val lastTrade: js.UndefOr[Double], // l1 - Last Trade (Price Only)
                   val highLimit: js.UndefOr[Double], // l2 - High Limit
                   val lowLimit: js.UndefOr[Double], // l3 - Low Limit
                   // m0 - Day's Range
                   // m2 - Day's Range (Real-time)
                   val movingAverage50Day: js.UndefOr[Double], // m3 - 50-day Moving Average
                   val movingAverage200Day: js.UndefOr[Double], // m4 - 200-day Moving Average
                   val change200DayMovingAvg: js.UndefOr[Double], //m5 - Change From 200-day Moving Average
                   val changePct200DayMovingAvg: js.UndefOr[Double], // m6 - Percent Change From 200-day Moving Average
                   val change50DayMovingAvg: js.UndefOr[Double], // m7 - Change From 50-day Moving Average
                   val changePct50DayMovingAvg: js.UndefOr[Double], // m8 - Percent Change From 50-day Moving Average
                   val name: js.UndefOr[String], // n0 - Name
                   val notes: js.UndefOr[String], // n4 - Notes
                   val open: js.UndefOr[Double], // o0 - Open
                   val prevClose: js.UndefOr[Double], // p0 - Previous Close
                   val pricePaid: js.UndefOr[Double], // p1 - Price Paid
                   val changePct: js.UndefOr[Double], // p2 - Change in Percent
                   val priceOverSales: js.UndefOr[Double], // p5 - Price/Sales
                   val priceOverBook: js.UndefOr[Double], // p6 - Price/Book
                   val exDividendDate: js.UndefOr[js.Date], // q0 - Ex-Dividend Date
                   // q1 - ????
                   val close: js.UndefOr[Double], // q2 - Close
                   val peRatio: js.UndefOr[Double], // r0 - P/E Ratio
                   val dividendPayDate: js.UndefOr[js.Date], // r1 - Dividend Pay Date
                   val peRatioRealTime: js.UndefOr[Double], // r2 - P/E Ratio (Real-time)
                   val pegRatio: js.UndefOr[Double], // r5 - PEG Ratio (Price Earnings Growth)
                   val priceOverEPSCurYr: js.UndefOr[Double], // r6 - Price/EPS Estimate Current Year
                   val priceOverEPSNextYr: js.UndefOr[Double], // r7 - Price/EPS Estimate Next Year
                   val oldSymbol: js.UndefOr[String], // s0 - Symbol
                   val newSymbol: js.UndefOr[String], // (Derived from e1: error message)
                   val sharesOwned: js.UndefOr[String], // s1 - Shares Owned
                   val revenue: js.UndefOr[Double], // s6 - Revenue
                   val shortRatio: js.UndefOr[Double], // s7 - Short Ratio
                   val tradeTime: js.UndefOr[String], // t1 - Last Trade Time
                   // t6 - Trade Links
                   // t7 - Ticker Trend
                   val target1Yr: js.UndefOr[Double], // t8 - 1-Year Target Price
                   val volume: js.UndefOr[Double], // v0 - Volume
                   val holdingsValue: js.UndefOr[Double], // v1 - Holdings Value
                   val holdingsValueRealTime: js.UndefOr[Double], // v7 - Holdings Value (Real-time)
                   // w0 - 52-week Range
                   val daysChange: js.UndefOr[Double], // w1 - Day's Value Change
                   val daysChangeRealTime: js.UndefOr[Double], // w4 - Day's Value Change (Real-time)
                   val exchange: js.UndefOr[String], // x0 - Stock Exchange
                   val divYield: js.UndefOr[Double], // y0 - Dividend Yield (Trailing Annual)
                   val responseTimeMsec: Double) extends js.Object

  val CODE_TO_FIELD_MAPPING = Map(
    "a0" -> "ask",
    "a2" -> "avgVol",
    "a5" -> "askSize",
    "b0" -> "bid",
    // b1 - ???? Ask/Bid?
    "b2" -> "askRealTime",
    "b3" -> "bidRealTime",
    "b4" -> "bookValuePerShare",
    "b6" -> "bidSize",
    "c0" -> "changePct", // Change & Percent Change
    "c1" -> "change",
    "c3" -> "commission",
    "c4" -> "currencyCode", // Currency Code (e.g. "USD")
    "c6" -> "changeRealTime",
    "c8" -> "changeAfterHours",
    "d0" -> "divShare", // Dividend/Share (Trailing Annual)
    "d1" -> "tradeDate", // Last Trade Date
    // d2 - Trade Date? (-)
    // d3 - Last Trade Time? ("Feb  3", "10:56am")
    "e0" -> "eps", // Earnings/Share (Diluted)
    "e1" -> "errorMessage", // Error Indication (returned for symbol changed / invalid)
    "e7" -> "epsEstCurrentYear", // EPS Estimate Current Year
    "e8" -> "epsEstNextYear", // EPS Estimate Next Year
    "e9" -> "epsEstNextQtr", // EPS Estimate Next Quarter
    "f6" -> "floatShares",
    "g0" -> "low", // Day's Low
    "g1" -> "holdingsGainPct",
    "g3" -> "annualizedGain",
    "g4" -> "holdingsGain",
    "g5" -> "holdingsGainPctRealTime",
    "g6" -> "holdingsGainRealTime",
    "h0" -> "high", // Day's High
    "i0" -> "moreInfo",
    "i5" -> "orderBookRealTime",
    "j0" -> "low52Week",
    "j1" -> "marketCap",
    "j2" -> "sharesOutstanding",
    "j3" -> "marketCapRealTime",
    "j4" -> "EBITDA",
    "j5" -> "change52WeekLow",
    "j6" -> "changePct52WeekLow",
    "k0" -> "high52Week",
    // k1 - Last Trade With Time (Real-time)
    "k2" -> "changePctRealTime",
    "k3" -> "lastTradeSize",
    "k4" -> "change52WeekHigh",
    "k5" -> "changePct52WeekHigh",
    // l0 - Last Trade (With Time)
    "l1" -> "lastTrade", // Last Trade (Price Only)
    "l2" -> "highLimit",
    "l3" -> "lowLimit",
    "m0" -> "daysRange",
    // m2 - Day's Range (Real-time)
    "m3" -> "movingAverage50Day", // 50-day Moving Average
    "m4" -> "movingAverage200Day", // 200-day Moving Average
    "m5" -> "change200DayMovingAvg", // Change From 200-day Moving Average
    "m6" -> "changePct200DayMovingAvg",
    "m7" -> "change50DayMovingAvg", // Change From 50-day Moving Average
    "m8" -> "changePct50DayMovingAvg",
    "n0" -> "name",
    "n4" -> "notes",
    "o0" -> "open",
    "p0" -> "prevClose",
    "p1" -> "pricePaid",
    "p2" -> "changePct",
    "p5" -> "priceOverSales", // Price/Sales
    "p6" -> "priceOverBook", // Price/Book
    "q0" -> "exDividendDate",
    // q1 - ????
    "q2" -> "close",
    "r0" -> "peRatio", // P/E Ratio
    "r1" -> "dividendPayDate",
    "r2" -> "peRatioRealTime",
    "r5" -> "pegRatio", //  Price/Earnings Growth Ratio
    "r6" -> "priceOverEPSCurYr", // Price/EPS Estimate Current Year
    "r7" -> "priceOverEPSNextYr", //  - Price/EPS Estimate Next Year
    "s0" -> "symbol",
    "s1" -> "sharesOwned",
    "s6" -> "revenue",
    "s7" -> "shortRatio",
    "t1" -> "tradeTime", // Last Trade Time (hh:mm)
    // t6 - Trade Links
    // t7 - Ticker Trend
    "t8" -> "target1Y", //  1-Year Target Price
    "v0" -> "volume", // Volume
    "v1" -> "holdingsValue", //  Holdings Value
    "v7" -> "holdingsValueRealTime",
    // w0 - 52-week Range
    // w1 - Day's Value Change
    // w4 - Day's Value Change (Real-time)
    "x0" -> "exchange",
    "y0" -> "divYield") // Dividend Yield (Trailing Annual)

  val FIELD_CODE_TO_MAPPING = CODE_TO_FIELD_MAPPING map { case (k, v) => (v, k) }

}