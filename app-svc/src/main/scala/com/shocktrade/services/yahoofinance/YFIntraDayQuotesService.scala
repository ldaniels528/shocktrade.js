package com.shocktrade.services.yahoofinance

import java.util.Date

import com.shocktrade.services.HttpUtil
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.language.postfixOps

/**
 * Yahoo! Finance Intra-Day Quotes Service
 * @author lawrence.daniels@gmail.com
 * @see http://chartapi.finance.yahoo.com/instrument/1.0/GOOG/chartdata;type=quote;range=1d/csv
 */
object YFIntraDayQuotesService extends HttpUtil {
  private lazy val logger = LoggerFactory.getLogger(getClass)

  /**
   * Retrieves intra-day quotes for the given symbol and period
   * @param symbol the given symbol (e.g. "AAPL")
   * @param range the given period in days
   * @return a sequence of [[YFIntraDayQuote intra-day quotes]]
   */
  def getQuotes(symbol: String, range: Int = 1): Seq[YFIntraDayQuote] = {
    // get the content as bytes
    val bytes = getResource(s"http://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=${range}d/csv")
    val lines = Source.fromBytes(bytes).getLines()

    /*
     * uri:/instrument/1.0/GOOG/chartdata;type=quote;range=1d/csv
     * ticker:goog
     * Company-Name:Google Inc.
     * Exchange-Name:NMS
     * unit:MIN
     * timezone:EDT
     * currency:USD
     * gmtoffset:-14400
     * previous_close:539.2700
     * Timestamp:1432215000,1432238400
     * labels:1432216800,1432220400,1432224000,1432227600,1432231200,1432234800,1432238400
     * values:Timestamp,close,high,low,open,volume
     */
    val header = lines.takeWhile(line => !line.startsWith("values:"))
    header.foreach(logger.debug)

    /*
     * close:536.2350,543.5200
     * high:536.4100,543.7900
     * low:535.9800,543.3600
     * open:536.1200,543.5200
     * volume:0,185400
     */
    val closingInfo = lines.takeWhile(line => !line.startsWith("volume:"))
    closingInfo.foreach(logger.debug)

    // transform the data
    // 1432215059,537.2100,537.3200,536.9000,536.9000,40800
    lines map { line =>
      val data = line.split("[,]")
      YFIntraDayQuote(
        timestamp = new Date(data.head.toLong * 1000L),
        close = data(4).toDouble,
        high = data(2).toDouble,
        low = data(3).toDouble,
        open = data(1).toDouble,
        volume = data(5).toLong
      )
    } toSeq
  }

  case class YFIntraDayQuote(timestamp: Date,
                             close: Double,
                             high: Double,
                             low: Double,
                             open: Double,
                             volume: Long)

}
