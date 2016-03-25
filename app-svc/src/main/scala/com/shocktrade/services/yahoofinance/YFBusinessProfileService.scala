package com.shocktrade.services.yahoofinance

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.xml.{NodeSeq, XML}

/**
 * Yahoo! Finance: Business Profile Service
 * @author lawrence.daniels@gmail.com
 */
object YFBusinessProfileService extends YFWebService {
  private val DETAILS = Set("Index Membership:", "Sector:", "Industry:", "Full Time Employees:")

  /**
   * Retrieves the business profile for the given symbol
   * @param symbol the given option symbol (e.g. "GOOG")
   * @return a { @link Future future} of a { @link YFOptionQuote quote}
   */
  def getProfile(symbol: String)(implicit ec: ExecutionContext): Future[YFProfile] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q/pr?s=$symbol+Profile")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long) = {
    val responseTimeMsec = System.currentTimeMillis() - startTime
    val details = parseProfileDetails(doc)
    YFProfile(
      symbol,
      details.get("Index Membership:"),
      details.get("Sector:"),
      details.get("Industry:"),
      details.get("Full Time Employees:") flatMap asNumber map (_.toInt),
      parseKeyExecutives(doc),
      parseBusinessSummary(doc),
      responseTimeMsec)
  }

  private def parseKeyExecutives(doc: NodeSeq): Seq[YFKeyExecutive] = {
    (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "yfnc_datamodoutline1")) {
        // iterate the rows
        val rows = table \ "tr" \ "td" \ "table" \ "tr"
        val headerCols = (rows.head \ "td") map (_.text.trim) map (s => if (s.length < 3) "Name" else s)
        if (headerCols.length == 3) {
          rows.tail flatMap { row =>
            val dataCols = (row \ "td") map (_.text)
            val m = Map(headerCols zip dataCols: _*)
            Some(toKeyExecutive(m))
          }
        } else None
      } else Seq.empty
    }
  }

  private def parseProfileDetails(doc: NodeSeq): Map[String, String] = {
    val items = (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "yfnc_datamodoutline1")) {
        // iterate the rows
        (table \ "tr" \ "td" \ "table" \ "tr") flatMap { row =>
          ((row \ "td") map (_.text) sliding(2, 2) toSeq) flatMap {
            case Seq(a, b, _*) => if (DETAILS.contains(a)) Some((a, b)) else None
            case _ => None
          }
        }
      } else Seq.empty
    }
    Map(items: _*)
  }

  private def parseBusinessSummary(doc: NodeSeq): Option[String] = {
    ((doc \\ "table") flatMap { table =>
      if ((table \ "@id").exists(_.text == "yfncsumtab")) {
        ((table \ "tr" \ "td" \ "p") map (_.text)).headOption
      } else None
    }).headOption
  }

  private def toKeyExecutive(m: Map[String, String]): YFKeyExecutive = {
    // get the executive's name, age, and title
    val (name, age, title) = m.get("Name") match {
      case Some(s) =>
        s.split("[,]") map (_.trim) match {
          case Array(aName, anAge, aTitle, _*) => (Some(aName), Some(anAge), Some(aTitle))
          case Array(aName, ageTitle, _*) => (Some(aName), Some(ageTitle.take(2)), Some(ageTitle.substring(2)))
        }
      case None => (None, None, None)
    }

    YFKeyExecutive(
      name,
      age,
      title,
      m.get("Pay") flatMap asNumber,
      m.get("Exercised") flatMap asNumber)
  }

  /**
   * Represents a key executive
   */
  case class YFKeyExecutive(
                             name: Option[String],
                             age: Option[String],
                             title: Option[String],
                             pay: Option[Double],
                             exercised: Option[Double])

  /**
   * Represents a profile
   */
  case class YFProfile(
                        symbol: String,
                        indexMembership: Option[String],
                        sector: Option[String],
                        industry: Option[String],
                        fullTimeEmployees: Option[Int],
                        executives: Seq[YFKeyExecutive],
                        businessSummary: Option[String],
                        responseTimeMsec: Long)

}