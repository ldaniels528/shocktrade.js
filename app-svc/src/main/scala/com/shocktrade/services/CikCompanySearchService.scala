package com.shocktrade.services

import com.shocktrade.services.util.ParsingUtilities._

import scala.xml.{NodeSeq, XML}

/**
 * U.S. SEC CIK Company Search Service
 * @author lawrence.daniels@gmail.com
 * @see http://www.sec.gov/cgi-bin/cik.pl.c?company=GREEN+BALLAST
 */
object CikCompanySearchService {

  /**
   * Returns a sequence of companies (including CIK code) that match the given company name
   */
  def search(companyName: String): Seq[CikInfo] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // convert the company name to a search term string
    val searchTerm = getSearchTerm(companyName)

    // retrieve the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.sec.gov/cgi-bin/cik.pl.c?company=$searchTerm")

    parseDocument(doc)
  }

  /**
   * Transform the document into a sequence of CIK information objects
   */
  private def parseDocument(doc: NodeSeq): Seq[CikInfo] = {
    (doc \\ "table" \ "tr" \ "td" \ "pre") flatMap { node =>
      val preTag = node.toString()
      for {
      // get the bounds of the <a> tag
        (a0, a1) <- preTag.findMatches("<a", "</a>")

        // get the anchor test
        anchor = preTag.substring(a0, a1)

        // get the bounds of the <a> tag's content
        (b0, b1) <- anchor.tagContent("a")

        // get the CIK code and company name
        code = anchor.substring(b0, b1).trim
        companyName = preTag.substring(a1, findEndOfName(preTag, a1)).trim
      } yield CikInfo(code, companyName)
    }
  }

  /**
   * Finds the limiting boundary of the company name
   */
  private def findEndOfName(line: String, p0: Int) = {
    // is there a carriage return? (\r\n)
    line.optionOf("\n", p0) match {
      case Some(index) => index
      case None => line.length - "</pre>".length
    }
  }

  case class CikInfo(cikNumber: String, cikName: String)

  /**
   * Convert the given company name into a search term string
   * @param companyName the given company name (e.g. "GREEN BALLAST")
   * @return the search term string (e.g. "GREEN+BALLAST")
   */
  def getSearchTerm(companyName: String) = {
    val set = Set("ADR", "CO", "INC", "LTD", "GRP", "THE")
    val map = Map("GRP" -> "GROUP", "HLDGS" -> "HOLDINGS", "AB" -> "", "CORP" -> "", "SA" -> "")
    withoutSymbols(companyName.toUpperCase) split "[ ]" map (_.trim) map (s => map.getOrElse(s, s)) filter (_.nonEmpty) filterNot (s => set.contains(s)) mkString "+"
  }

  /**
   * Removes all non-alphanumeric characters
   */
  private[this] def withoutSymbols(s: String) = {
    s map {
      case c if c.isDigit || c.isLetter || c == ' ' => c
      case _ => ' '
    }
  }

}