package com.shocktrade.services.currency

import java.util.Date

import scala.xml.{Node, NodeSeq, XML}

/**
 * FloatRates.com Currency Service
 * @author lawrence.daniels@gmail.com
 */
object FloatRatesCurrencyService {

  /**
   * Parses currencies from FloatRates.com
   * @see http://www.floatrates.com/daily/USD.xml
   */
  def getQuotes: Seq[FRCurrency] = {
    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML.load("http://www.floatrates.com/daily/USD.xml")
    parseDocument(doc, startTime)
  }

  private def parseDocument(doc: NodeSeq, startTime: Long): Seq[FRCurrency] = {
    import java.text.SimpleDateFormat

    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // create the date parser
    val sdf = new SimpleDateFormat("E',' dd MMM yyyy hh:mm:ss z")

    // parse the document
    (doc \\ "channel" \ "item") map { node =>
      // transform the node into a quote
      FRCurrency(
        extract(node, "title"),
        extract(node, "description"),
        extract(node, "pubDate") map sdf.parse,
        extract(node, "baseCurrency"),
        extract(node, "targetCurrency"),
        extract(node, "exchangeRate") flatMap asDecimal,
        responseTimeMsec)
    }
  }

  private def asDecimal(s: String): Option[Double] = {
    val value = s.replaceAll("[,]", "").trim
    if (value.length > 0) Some(value.toDouble) else None
  }

  private def extract(node: Node, name: String): Option[String] = {
    (node \ name).map(_.text).headOption
  }

  case class FRCurrency(title: Option[String],
                        description: Option[String],
                        pubDate: Option[Date],
                        baseCurrency: Option[String],
                        targetCurrency: Option[String],
                        exchangeRate: Option[Double],
                        responseTimeMsec: Long)

}