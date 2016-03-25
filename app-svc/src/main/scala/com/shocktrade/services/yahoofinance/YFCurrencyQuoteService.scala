package com.shocktrade.services.yahoofinance

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

/**
 * Yahoo! Finance: Currency Quote Service
 * @author lawrence.daniels@gmail.com
 */
object YFCurrencyQuoteService extends YFWebService {

  /**
   * Retrieves for a quote for the given currency symbol
   * @param symbol the given currency symbol (e.g. "EUR=X")
   * @return a { @link Future future} of a { @link YFCurrencyQuote quote}
   */
  def getQuote(symbol: String)(implicit ec: ExecutionContext): Future[YFCurrencyQuote] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q/op?s=$symbol+Options")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long) = {
    // extract the table data as key-value pairs
    val kvps = (doc \\ "table") flatMap parseTableData

    // transform the key-value pairs into a quote
    toQuote(symbol, kvps, startTime)
  }

  /**
   * Transforms the given sequence of key-value pairs into a currency quote
   */
  private def toQuote(symbol: String, pairs: Seq[(String, String)], startTime: Long): YFCurrencyQuote = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the data
    val m = Map(pairs: _*)
    val (daysLow, daysHigh) = numberTuple(m.get("Day's Range:"))
    val (_52wLow, _52wHigh) = numberTuple(m.get("52wk Range:"))

    // create the quote
    YFCurrencyQuote(
      symbol.toUpperCase,
      m.get("Prev Close:") flatMap asNumber,
      m.get("Open:") flatMap asNumber,
      m.get("Ask:") flatMap asNumber,
      m.get("Bid:") flatMap asNumber,
      daysLow,
      daysHigh,
      toSpread(daysHigh, daysLow),
      _52wLow,
      _52wHigh,
      responseTimeMsec)
  }

  /**
   * Transforms the given node sequence into a sequence of key-value pairs
   */
  private def parseTableData(table: NodeSeq): Seq[(String, String)] = {
    if ((table \\ "@id").exists(n => n.text == "table1" || n.text == "table2")) {
      val rows = table \\ "tr"
      rows map { row =>
        val label = (row \\ "th").text
        val value = (row \\ "td").text
        (label, value)
      }
    } else Seq.empty
  }

  /**
   * Represents the retrieved currency quote
   * @author lawrence.daniels@gmail.com
   */
  case class YFCurrencyQuote(symbol: String,
                             prevClose: Option[Double],
                             open: Option[Double],
                             ask: Option[Double],
                             bid: Option[Double],
                             low: Option[Double],
                             high: Option[Double],
                             spread: Option[Double],
                             low52Week: Option[Double],
                             high52Week: Option[Double],
                             responseTimeMsec: Long)

}