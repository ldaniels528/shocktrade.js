package com.shocktrade.services

import scala.xml.{NodeSeq, XML}

/**
 * iShares: Holdings Detail Service
 * @author lawrence.daniels@gmail.com
 */
object ISharesHoldingsDetailService {
  private val DECIMAL_r = "(\\d+\\.\\d*)".r

  /**
   * Retrieves for the holdings for the given ETF symbol
   * @param symbol the given ETF symbol (e.g. "HDV")
   * @return a { @link Future future} of a { @link YFCurrencyQuote quote}
   */
  def getHoldings(symbol: String): Seq[Holding] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://us.ishares.com/product_info/fund/holdings/$symbol.htm")
    parseDocument(doc, startTime)
  }

  private def parseDocument(doc: NodeSeq, startTime: Long): Seq[Holding] = {
    (doc \\ "table") flatMap (parseTableData(_, startTime))
  }

  private def parseTableData(table: NodeSeq, startTime: Long): Seq[Holding] = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the sub-document
    if ((table \\ "@id").exists(_.text == "holdings-eq")) {
      // extract each row of holdings (skip the header row)
      (table \\ "tr").tail map (row => toHolding(row \\ "td", responseTimeMsec))
    } else Seq.empty
  }

  private def toHolding(nodes: NodeSeq, responseTimeMsec: Long) = {
    val m = Map(nodes map (n => ((n \ "@id").text.trim, n.text.trim)): _*)
    Holding(
      m.get("holding-ticker"),
      m.get("holding-nm"),
      m.get("net-assets-pct") map {
        currencyCleanUp
      },
      m.get("market-value") map {
        currencyCleanUp
      },
      m.get(""), // ISIN
      m.get("market-nm"),
      m.get("fundSectorName"),
      m.get("fx-rate") map {
        currencyCleanUp
      },
      responseTimeMsec)
  }

  private def currencyCleanUp(s: String) = s.replaceAll("[$]", "").replaceAll("[,]", "").toDouble

  /**
   * Represents the retrieved holding
   * @author lawrence.daniels@gmail.com
   */
  case class Holding(symbol: Option[String],
                     name: Option[String],
                     netAssetsPct: Option[Double],
                     marketValue: Option[Double],
                     ISIN: Option[String],
                     market: Option[String],
                     sector: Option[String],
                     exchangeRate: Option[Double],
                     responseTimeMsec: Long)

}