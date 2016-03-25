package com.shocktrade.services

import scala.xml.{ NodeSeq, XML }

/**
 * Bloomberg Profile Service
 * @author lawrence.daniels@gmail.com
 */
object BloombergProfileService {

  /**
   * Retrieves for a profile for the given ticker symbol
   * @param symbol the given ticker symbol (e.g. "AEGY")
   * @return a [[BBProfile Bloomberg profile]]
   */
  def getProfile(symbol: String, country: String = "US"): BBProfile = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve the HTML document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.bloomberg.com/quote/$symbol:$country/profile")

    // parse the document
    parseDocument(symbol, doc, startTime)
  }

  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): BBProfile = {
    // capture the time it took to retrieve the document
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the data sections
    val m = parseExchangeSection(doc)

    // return the profile data
    BBProfile(
      symbol,
      parseName(doc),
      parseBusinessSummary(doc),
      m.get("exchange:"),
      m.get("sector:"),
      m.get("industry:"),
      m.get("sub-industry:"),
      responseTimeMsec)
  }

  private def parseName(doc: NodeSeq): Option[String] = {
    //  <h2 itemprop="name">Kibush Capital Corp</h2>
    ((doc \\ "h2") flatMap { h2 =>
      if ((h2 \ "@itemprop").exists(_.text == "name")) Some(h2.text) else None
    }).headOption
  }

  private def parseBusinessSummary(doc: NodeSeq): Option[String] = {
    // <p id="extended_profile" itemprop="description">
    ((doc \\ "p") flatMap { p =>
      if ((p \ "@itemprop").exists(_.text == "description")) Some(p.text.trim) else None
    }).headOption
  }

  private def parseExchangeSection(doc: NodeSeq): Map[String, String] = {
    val tups = (doc \\ "div") flatMap { div =>
      if ((div \ "@class").exists(_.text == "exchange_type")) {
        (div \\ "li") flatMap { li =>
          val spans = (li \ "span") map (_.text.trim)
          spans.toList match {
            case k :: v :: Nil => Some((k.toLowerCase, v))
            case _ => None
          }
        }
      } else Seq.empty
    }
    Map(tups: _*)
  }

  /**
   * Represents a Bloomberg Profile
   * @author lawrence.daniels@gmail.com
   */
  case class BBProfile(
    symbol: String,
    name: Option[String],
    businessSummary: Option[String],
    exchange: Option[String],
    sector: Option[String],
    industry: Option[String],
    subIndustry: Option[String],
    responseTimeMsec: Long)

}