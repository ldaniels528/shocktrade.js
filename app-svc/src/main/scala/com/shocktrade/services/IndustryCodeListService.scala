package com.shocktrade.services

import scala.concurrent.ExecutionContext
import scala.xml.{NodeSeq, XML}

/**
 * U.S. SEC Corporation Finance Industry Assignment List Service
 * @author lawrence.daniels@gmail.com
 */
object IndustryCodeListService {

  /**
   * Retrieves a list of industry code assignments
   * @param page the given company name page (e.g. "a"..."t", "u-v", "w-z")
   * @return a sequence of [[IndustryCodeAssignment industry code assignments]]
   */
  def getCodeAssignments(page: String = "a")(implicit ec: ExecutionContext): Seq[IndustryCodeAssignment] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.sec.gov/divisions/corpfin/organization/cfia-$page.htm")

    parseDocument(doc, startTime)
  }

  /**
   * Parses the document generating a sequence of industry code assignments
   */
  private def parseDocument(doc: NodeSeq, startTime: Long): Seq[IndustryCodeAssignment] = {
    (doc \\ "table") flatMap { table =>
      if ((table \ "@id").exists(_.text == "cos")) {
        val rows = table \ "tr"

        // get the column headers
        val keys = (rows.head \ "th") map (_.text.trim)

        // create the data rows
        rows.tail map { row =>
          val values = (row \ "td") map (_.text.trim)
          val m = Map(keys zip values: _*)
          toIndustryCodeAssignment(m)
        }
      } else Seq.empty
    }
  }

  /**
   * Transforms the given map into an industry code assignment
   */
  private def toIndustryCodeAssignment(m: Map[String, String]) = {
    IndustryCodeAssignment(
      m.get("Company Name"),
      m.get("CIK Number") map (_.toInt),
      m.get("SIC Code") map (_.toInt))
  }

  /**
   * Represents an Industry Code Assignment
   */
  case class IndustryCodeAssignment(companyName: Option[String],
                                    cikNumber: Option[Int],
                                    sicNumber: Option[Int])

}