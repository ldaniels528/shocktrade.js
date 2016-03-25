package com.shocktrade.services.yahoofinance

import java.util.Date

import com.shocktrade.services.HttpUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

/**
 * Yahoo! Finance: Trading History Service
 * @see http://stackoverflow.com/questions/1316093/fetch-stock-quotes-from-google-finance-yahoo-finance-or-the-exchange-itself
 * @author lawrence.daniels@gmail.com
 */
object YFHistoricalQuoteService extends YFWebService with HttpUtil {

  /**
   * Retrieves historical quotes for the given symbol
   * @param symbol the given ticker (e.g. "AAPL")
   */
  def getQuotes(symbol: String)(implicit ec: ExecutionContext): Future[Seq[YFHistoricalQuote]] = {

    // capture the service start time
    val startTime = System.currentTimeMillis()

    for {
    // retrieve the CVS data as bytes
      doc <- Future {
        getResource(s"http://ichart.finance.yahoo.com/table.csv?s=$symbol")
      }

    // transform the document to quotes
    } yield parseDocument(symbol, doc, startTime)
  }

  private def parseDocument(symbol: String, doc: Array[Byte], startTime: Long): Seq[YFHistoricalQuote] = {
    // create the sequence of quotes
    val quotes = Source.fromBytes(doc).getLines map (toQuote(symbol, _))

    // sort the quotes
    quotes.toSeq.sortBy(_.tradeDate)
  }

  /**
   * Converts the line of CSV data into a historical quote
   */
  private def toQuote(symbol: String, line: String) = {
    // parse the line
    val pcs = line split ","

    // cache the day's high and low
    val daysHigh = extract(pcs, 2) flatMap asNumber
    val daysLow = extract(pcs, 3) flatMap asNumber

    // create the quote
    YFHistoricalQuote(
      symbol,
      extract(pcs, 0) flatMap (asDate(_, "yyyy-MM-dd")), // tradeDate
      extract(pcs, 1) flatMap asNumber, // open
      daysHigh, // high
      daysLow, // low
      toSpread(daysHigh, daysLow),
      extract(pcs, 4) flatMap asNumber, // close
      extract(pcs, 5) flatMap asLong, // volume
      extract(pcs, 6) flatMap asNumber) // adjClose
  }

  /**
   * Represents a historical quote
   */
  case class YFHistoricalQuote(symbol: String,
                               tradeDate: Option[Date],
                               open: Option[Double],
                               high: Option[Double],
                               low: Option[Double],
                               spread: Option[Double],
                               close: Option[Double],
                               volume: Option[Long],
                               adjClose: Option[Double])

}