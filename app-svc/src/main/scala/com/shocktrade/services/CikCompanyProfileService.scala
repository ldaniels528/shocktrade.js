package com.shocktrade.services

import scala.xml.{Node, NodeSeq, XML}

/**
 * U.S. SEC CIK Company Profile Service
 * @author lawrence.daniels@gmail.com
 * @see http://www.sec.gov/cgi-bin/cik.pl.c?company=GREEN+BALLAST
 */
object CikCompanyProfileService {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  /**
   * Retrieves the business profile by CIK number
   */
  def getProfile(cikNumber: String) = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.sec.gov/cgi-bin/own-disp?action=getissuer&CIK=$cikNumber")

    parseDocument(doc, startTime)
  }

  /**
   * Transform the document into a sequence of CIK information objects
   */
  private def parseDocument(doc: NodeSeq, startTime: Long) = {
    // iterate all rows of tables within "\\body\div"
    (doc \\ "body" \ "div" \ "table" \ "tr") map { row =>

      // process the columns
      (row \ "td") map (_.text.trim) foreach (s => logger.info(s"text: $s"))

      toCompanyProfile(row)
    }
  }

  private def toCompanyProfile(node: Node) = {

  }

  /**
   * Represents a Company Profile
   */
  case class CikCompanyProfile(
                                cikNumber: String,
                                companyName: String,
                                sicNumber: String,
                                state: String)

}