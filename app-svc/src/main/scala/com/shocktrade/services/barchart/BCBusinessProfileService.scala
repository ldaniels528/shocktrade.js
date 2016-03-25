package com.shocktrade.services.barchart

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.xml.{NodeSeq, XML}

/**
 * BarChart.com Business Profile Service
 * @see http://www.barchart.com/profile/stocks/PRGN
 * @author lawrence.daniels@gmail.com
 */
object BCBusinessProfileService {
  private[this] lazy val logger = LoggerFactory.getLogger(getClass)
  private val SIC_CODE_r = "(\\d+)".r
  private val SIC_DESC_r = "[SIC.\\d+] (\\S+)*".r

  /**
   * Retrieves for a quote for the given stock symbol
   * @param symbol the given stock symbol (e.g. "AAPL")
   * @return a [[BCBusinessProfile business profile]]
   */
  def getProfile(symbol: String)(implicit ec: ExecutionContext): BCBusinessProfile = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.barchart.com/profile/stocks/$symbol")

    parseDocument(symbol, doc, startTime)
  }

  /**
   * Parses the document into a business profile
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): BCBusinessProfile = {
    // capture the response time
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // extract the company/fund name
    // <h1 class="fl" id="symname">Berkshire Hath Hld B (BRK.B)</h1>
    val companyName = ((doc \\ "h1") flatMap { h1 =>
      if ((h1 \ "@id").exists(_.text == "symname")) Some(h1.text.trim) else None
    }).headOption

    // extract the profile data
    // <div class="mpbox"> .. </div>
    val results = (doc \\ "div") flatMap { div =>
      if ((div \ "@class").exists(_.text == "mpbox")) {
        // get the rows
        (div \ "table" \ "tr") flatMap { row =>
          (row \ "td") map (_.text.trim) match {
            case Seq(a, b, _*) =>
              //logger.info(s"'$a' ~> '$b'")
              Some((a, b))
            case _ => None
          }
        }
      } else Seq.empty
    }

    // transform the data into a business profile object
    toProfile(symbol, companyName, Map(results: _*), responseTimeMsec)
  }

  /**
   * Transforms the given mapping into a business profile
   */
  private def toProfile(symbol: String, companyName: Option[String], m: Map[String, String], responseTimeMsec: Long): BCBusinessProfile = {
    // parse the compound items
    val (sicCode, sicDesc) = parseSicCode(m.get("SIC Code:"))
    val (industry, sector, membership) = parseIndustrySector(m.get("Industry/Sector:"))

    // create the instance
    BCBusinessProfile(
      symbol,
      companyName map stripOffTicker,
      m.get("Ticker Symbol:"),
      m.get("Exchange:"),
      m.get("Contact Info:") map (_.split("\n") map (_.trim)),
      m.get("CEO / President:"),
      industry,
      sector,
      membership,
      sicCode,
      sicDesc,
      m.get("Description:"),
      responseTimeMsec)
  }

  /**
   * Return a new string that is absent of the ticker (eg. "Berkshire Hath Hld B (BRK.B)" ~> "Berkshire Hath Hld B")
   */
  private def stripOffTicker(s: String) = {
    val index = s.lastIndexOf("(")
    if (index != -1) s.substring(0, index - 1).trim else s
  }

  /**
   * Parses the SIC data into a tuple containing the code and description
   */
  private def parseSicCode(v: Option[String]): (Option[String], Option[String]) = {
    def extractDesc(s: String) = {
      val idx = s.indexOf(' ')
      if (idx != -1) Some(s.substring(idx + 1)) else None
    }

    v match {
      case Some(s) => (SIC_CODE_r.findFirstIn(s), extractDesc(s))
      case None => (None, None)
    }
  }

  /**
   * Parses the Industry/Sector data into a tuple of industry, sector, and index membership values
   */
  private def parseIndustrySector(option: Option[String]): (Option[String], Option[String], Set[String]) = {
    option match {
      case Some(text) =>
        // split the text into sections
        val pcs = text.split(",") map (_.trim)

        // the head contains the industry/sectors
        val (industry, sector) = pcs.headOption match {
          case None => (None, None)
          case Some(indusSecsText) =>
            val Array(industry, sector, _*) = indusSecsText.split("-") map (_.trim)
            (Some(industry), Some(sector))
        }

        // the tail contains the index membership
        val membership = pcs.tail.toSet

        // return the tuple
        (industry, sector, membership)
      case None => (None, None, Set.empty)
    }
  }

  /**
   * Represents a business profile
   */
  case class BCBusinessProfile(symbol: String,
                               companyName: Option[String],
                               ticker: Option[String],
                               exchange: Option[String],
                               contactInfo: Option[Seq[String]],
                               presidentCEO: Option[String],
                               industry: Option[String],
                               sector: Option[String],
                               membership: Set[String],
                               sicCode: Option[String],
                               sicDescription: Option[String],
                               businessSummary: Option[String],
                               responseTimeMsec: Long)

}