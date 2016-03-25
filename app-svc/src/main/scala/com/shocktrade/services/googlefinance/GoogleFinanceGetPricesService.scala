package com.shocktrade.services.googlefinance

import java.util.Date

import com.shocktrade.services.HttpUtil
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.language.postfixOps

/**
 * Google Finance GetPrices Service
 * @see http://www.google.com/finance/getprices?q=AAPL&x=NASD&i=120&sessions=ext_hours&p=5d&f=d,c,v,o,h,l&df=cpct&auto=1&ts=1324323553905
 * @author lawrence.daniels@gmail.com
 */
object GoogleFinanceGetPricesService extends HttpUtil {
  private[this] lazy val logger = LoggerFactory.getLogger(getClass)

  /**
   * Retrieves the intra-day quotes for the given symbol and time period
   * @param request the given [[GfGetPricesRequest intra-day data request]]
   * @return a collection of [[GfPriceQuote intra-day quotes]]
   */
  def getQuotes(request: GfGetPricesRequest): List[GfPriceQuote] = {
    // get the content as bytes
    val bytes = getResource(createServiceURL(request))
    val lines = Source.fromBytes(bytes).getLines()
    val header = extractHeaderInfo(lines)
    var initTs = System.currentTimeMillis()
    lines map toRawData map { q =>
      // compute the timestamp
      val time = q.ts match {
        case s if s.startsWith("a") =>
          initTs = s.substring(1).toLong * 1000L
          initTs
        case s => initTs + s.toLong * 1000L
      }

      // return the quote
      GfPriceQuote(time = new Date(time), close = q.close, high = q.high, low = q.low, open = q.open, volume = q.volume)
    } toList
  }

  private def createServiceURL(request: GfGetPricesRequest) = {
    val i = request.intervalInSecs
    val p = s"${request.periodInDays}d"
    val q = request.symbol
    val ts = request.startTime
    //val x = getExchangeCode(request.exchange)
    //df = difference? or data format - compact? df=cpct
    //s"http://www.google.com/finance/getprices?q=$q&x=$x&i=$i&sessions=ext_hours&p=$p&f=d,c,v,o,h,l&df=cpct&auto=1&ts=$ts"
    s"http://www.google.com/finance/getprices?q=$q&i=$i&p=$p&f=d,c,v,o,h,l&ts=$ts"
  }

  private def getExchangeCode(exchange: String) = {
    exchange.toUpperCase match {
      case "NASDAQ" => "NASD"
      case s => s
    }
  }

  private def extractHeaderInfo(lines: Iterator[String]) = {
    /**
     * Skip the header information
     *
     * EXCHANGE%3DNASDAQ
     * MARKET_OPEN_MINUTE=570
     * MARKET_CLOSE_MINUTE=960
     * INTERVAL=120
     * COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
     * DATA_SESSIONS=[PREMARKET,240,570],[AFTER_HOURS,960,1200]
     * DATA=
     * TIMEZONE_OFFSET=-240
     */
    val header = lines.takeWhile(line => !line.contains("TIMEZONE_OFFSET"))
    header foreach logger.info
    header
  }

  private def toRawData(line: String) = {
    // parse the line of text (e.g. "195,128.88,128.95,128.88,128.94,3438")
    val data = line.split("[,]")

    GfGetPricesData(
      data(0),
      data(1).toDouble,
      data(2).toDouble,
      data(3).toDouble,
      data(4).toDouble,
      data(5).toLong
    )
  }

  /**
   * Represents the Google Finance GetPrices API input parameters
   * @param symbol the given ticker symbol (q)
   * @param startTime the start time as a UNIX timestamp
   * @param intervalInSecs the data interval in seconds (i)
   * @param periodInDays the time period (a number followed by a "d" or "Y", eg. Days or years. Ex: 40Y = 40 years.)
   * @see http://www.networkerror.org/component/content/article/44-googles-undocumented-finance-api.html
   */
  case class GfGetPricesRequest(symbol: String,
                                startTime: Long = System.currentTimeMillis() / 1000L,
                                intervalInSecs: Int = 86400,
                                periodInDays: Int = 1)

  case class GfGetPricesData(ts: String,
                             close: Double,
                             high: Double,
                             low: Double,
                             open: Double,
                             volume: Long)

  case class GfPriceQuote(time: Date,
                          close: Double,
                          high: Double,
                          low: Double,
                          open: Double,
                          volume: Long)

}
