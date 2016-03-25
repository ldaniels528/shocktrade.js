package com.shocktrade.services

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

/**
 * OTCBB Profile Service
 * @author lawrence.daniels@gmail.com
 */
object OTCBBProfileService {

  /**
   * Retrieves for a real-time quote for the given OTC/BB ticker symbol
   * @param symbol the given ticker symbol (e.g. "AEGY")
   * @return a { @link Future future} of a { @link OTCBBProfile OTC/BB profile}
   */
  def getProfile(symbol: String)(implicit ec: ExecutionContext): Future[OTCBBProfile] = {
    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://www.otcbb.com/profiles/$symbol.htm")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Retrieves for a real-time quote for the given OTC/BB ticker symbol
   * @param symbol the given ticker symbol (e.g. "AEGY")
   * @return a { @link OTCBBProfile OTC/BB profile}
   */
  def getProfileSync(symbol: String): OTCBBProfile = {
    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.otcbb.com/profiles/$symbol.htm")

    parseDocument(symbol, doc, startTime)
  }

  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): OTCBBProfile = {
    val m = parseBusinessSummary(symbol, doc) ++ parseCompanyInfo(doc)
    //m foreach { case (k, v) => System.out.println(s"$k => $v") }

    OTCBBProfile(
      symbol,
      m.get("SIC Number") map (_.toInt),
      m.get("CIK"),
      m.get("Fiscal Year End"),
      m.get("Phone"),
      m.get("Transfer Agent"),
      m.get("Business Summary"),
      System.currentTimeMillis() - startTime)
  }

  /**
   * Parses company information
   * <td class='copy'>
   * SIC Number: 2860<br>
   * Fiscal Year End: 07-31<br>
   * Transfer Agent: Olde Monmouth Stock Transfer Co Inc<br>
   * CIK: 1446896<br>
   * </td>
   */
  private def parseCompanyInfo(doc: NodeSeq): Map[String, String] = {
    Map((doc \\ "td") flatMap { col =>
      if ((col \ "@class").exists(_.text == "copy")) {
        val values = col.child map (_.text) flatMap {
          case s if s.contains(':') => s.split("[:]") map (_.trim) match {
            case Array(k, v) => Some((k, v))
            case _ => None
          }
          case _ => None
        }
        values
      } else Seq.empty
    }: _*)
  }

  private def parseBusinessSummary(symbol: String, doc: NodeSeq): Map[String, String] = {
    Map((doc \\ "td") flatMap { col =>
      if ((col \ "@class").exists(_.text == "copy") && (col \ "@colspan").exists(_.text == "3") &&
        (col.text.length > 50) && col.text.contains(symbol)) {
        Some(("Business Summary", col.text))
      } else None
    }: _*)
  }

  /**
   * Represents an OTCBB Profile
   * @author lawrence.daniels@gmail.com
   */
  case class OTCBBProfile(symbol: String,
                          sicNumber: Option[Int],
                          cikNumber: Option[String],
                          fiscalYearEnd: Option[String],
                          contactPhone: Option[String],
                          transferAgent: Option[String],
                          businessSummary: Option[String],
                          responseTimeMsec: Long)

}