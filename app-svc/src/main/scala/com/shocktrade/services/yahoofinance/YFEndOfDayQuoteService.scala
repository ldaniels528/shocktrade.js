package com.shocktrade.services.yahoofinance

import java.text.SimpleDateFormat
import java.util.Date

import com.shocktrade.services.HttpUtil

import scala.io.Source
import scala.language.postfixOps

/**
 * Yahoo! Finance End-Of-Day Quote Service
 * @see http://real-chart.finance.yahoo.com/table.csv?s=YHOO&a=04&b=20&c=2015&d=04&e=22&f=2015&g=d&ignore=.csv
 * @author lawrence.daniels@gmail.com
 */
object YFEndOfDayQuoteService extends HttpUtil {

  def getQuotes(symbol: String, startDate: Date, endDate: Date): Seq[YFEndOfDayQuote] = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val bytes = getResource(buildURL(symbol, startDate, endDate), "Content-Type" -> "text/csv")
    val lines = Source.fromBytes(bytes).getLines()

    // skip the header line
    lines.next()
    lines map { line =>
      val data = line.split("[,]")
      YFEndOfDayQuote(
        tradeDate = sdf.parse(data.head),
        open = data(1).toDouble,
        high = data(2).toDouble,
        low = data(3).toDouble,
        close = data(4).toDouble,
        volume = data(5).toLong,
        adjClose = data(6).toDouble
      )
    } toSeq
  }

  private def buildURL(symbol: String, startDate: Date, endDate: Date): String = {
    val sdf = new SimpleDateFormat("MMddyyyy")

    // parse the start date
    val start = sdf.format(startDate)
    val (a, b, c) = (start.substring(0, 2), start.substring(2, 4), start.substring(4, 8))

    // parse the end date
    val end = sdf.format(endDate)
    val (d, e, f) = (end.substring(0, 2), end.substring(2, 4), end.substring(4, 8))

    // build the URL
    s"http://real-chart.finance.yahoo.com/table.csv?s=$symbol&a=$a&b=$b&c=$c&d=$d&e=$e&f=$f&g=d&ignore=.csv"
  }

  case class YFEndOfDayQuote(tradeDate: Date,
                             open: Double,
                             high: Double,
                             low: Double,
                             close: Double,
                             volume: Long,
                             adjClose: Double)

}
