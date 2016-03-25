package com.shocktrade.services.googlefinance

import java.text.SimpleDateFormat
import java.util.Date

import com.shocktrade.services.{HttpUtil, TextParsing}

import scala.io.Source
import scala.language.postfixOps

/**
 * Google Finance Trading History Service
 * @see http://stackoverflow.com/questions/527703/how-can-i-get-stock-quotes-using-google-finance-api
 * @author lawrence.daniels@gmail.com
 */
object GoogleFinanceTradingHistoryService extends TextParsing with HttpUtil {

  /**
   * Retrieves the trading history for the given symbol
   */
  def getTradingHistory(symbol: String, startDate: Date, endDate: Date = new Date): Seq[GFHistoricalQuote] = {
    // get the content as bytes
    val bytes = getResource(serviceTradingHistoryURL(symbol, startDate, endDate))

    // get an iteration of the lines
    val lines = Source.fromBytes(bytes).getLines()

    // skip the header line
    lines.next()

    // convert all other lines to historical quotes
    val quotes = lines map (parseTradingHistoryQuote(symbol, _)) toSeq

    // sort the quotes and update the previous close
    quotes.sortBy(_.tradeDate)
  }

  /**
   * Creates the Google Finance Trading History service URL
   */
  private def serviceTradingHistoryURL(symbol: String, start: Date, end: Date) = {
    val df = new SimpleDateFormat("MMM+dd,yyyy")
    //"http://finance.google.com/finance/historical?q=%s&startdate=%s&enddate=%s&output=csv"
    "http://finance.google.com/finance/historical?q=%s&startdate=%s&output=csv".format(symbol, df.format(start), df.format(end))
  }

  /**
   * Parses a line of CSV trading history data
   * @param symbol the given ticker symbol
   * @param line the given line of text to be parsed
   * @return a new { @link HistoricalQuote trading history} object
   */
  private def parseTradingHistoryQuote(symbol: String, line: String) = {
    // parse the line
    val pcs = line split "[,]"

    // extract the values
    val tradeDate = extract(pcs, 0) flatMap (asDate(_, "dd-MMM-yy"))
    val open = extract(pcs, 1) flatMap asNumber
    val high = extract(pcs, 2) flatMap asNumber
    val low = extract(pcs, 3) flatMap asNumber
    val spread = toSpread(high, low)
    val close = extract(pcs, 4) flatMap asNumber
    val volume = extract(pcs, 5) flatMap asLong

    // create the quote
    GFHistoricalQuote(symbol, tradeDate, open, high, low, spread, close, volume)
  }

  /**
   * Represents a Google Finance Historical Quote
   */
  case class GFHistoricalQuote(symbol: String,
                               tradeDate: Option[Date],
                               open: Option[Double],
                               high: Option[Double],
                               low: Option[Double],
                               spread: Option[Double],
                               close: Option[Double],
                               volume: Option[Long])

}
