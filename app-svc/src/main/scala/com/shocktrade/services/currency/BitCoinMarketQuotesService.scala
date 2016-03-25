package com.shocktrade.services.currency

import scala.concurrent.ExecutionContext
import scala.xml.{NodeSeq, XML}

/**
 * BitCoinWatch.com Market Quotes Service
 * @author lawrence.daniels@gmail.com
 */
object BitCoinMarketQuotesService {

  /**
   * Retrieves a collection of BitCoin quotes for all available markets
   */
  def getQuotes()(implicit ec: ExecutionContext): Seq[BitCoinMarketQuote] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document

    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load("http://bitcoinwatch.com/")

    parseDocument(doc, startTime)
  }

  private def parseDocument(doc: NodeSeq, startTime: Long): Seq[BitCoinMarketQuote] = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the document
    (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "data")) {
        val rows = (table \ "tr")

        // first row contains the headers
        val headers = (rows.head \ "th") map (_.text)
        if (headers.length == 7) {
          // extract each row of holdings (skip the header row)
          rows.tail map { row =>
            val data = (row \ "td") map (_.text)
            val m = Map(headers zip data: _*)
            toQuote(m, responseTimeMsec)
          }
        } else Seq.empty
      } else Seq.empty
    }
  }

  private def toQuote(m: Map[String, String], responseTimeMsec: Long): BitCoinMarketQuote = {
    BitCoinMarketQuote(
      m.get("Market"),
      m.get("Last") map (cleanse) map (_.toDouble),
      m.get("Volume (24h)") map (cleanse) map (_.toDouble),
      m.get("Bid") map (cleanse) map (_.toDouble),
      m.get("Ask") map (cleanse) map (_.toDouble),
      m.get("High") map (cleanse) map (_.toDouble),
      m.get("Low") map (cleanse) map (_.toDouble),
      responseTimeMsec)
  }

  private def cleanse(s: String) = s.replaceAll("[,]", "")

  case class BitCoinMarketQuote(
                                 market: Option[String],
                                 lastTrade: Option[Double],
                                 volume: Option[Double],
                                 bid: Option[Double],
                                 ask: Option[Double],
                                 high: Option[Double],
                                 low: Option[Double],
                                 responseTimeMsec: Long)

}