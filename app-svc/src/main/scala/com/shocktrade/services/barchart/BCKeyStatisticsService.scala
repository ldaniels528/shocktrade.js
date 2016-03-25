package com.shocktrade.services.barchart

import java.text.SimpleDateFormat
import java.util.Date

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}

/**
 * BarChart.com Key Statistics Service
 * @see http://www.barchart.com/profile.php?sym=BRK.B&view=key_statistics
 * @author lawrence.daniels@gmail.com
 */
object BCKeyStatisticsService {

  /**
   * Retrieves a quote for the given ticker symbol
   * @param symbol the given ticker symbol
   */
  def getKeyStatistics(symbol: String)(implicit ec: ExecutionContext): Future[BCKeyStatistics] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://www.barchart.com/profile.php?sym=$symbol&view=key_statistics")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Transforms the document into a key statistics instance
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): BCKeyStatistics = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // extract the key statistics data
    val m = parseKeyStatistics(doc)

    // Transforms the given mapping into a key statistics instance
    new BCKeyStatistics(
      symbol,
      parseCompanyName(doc),
      parseAsOfDate(doc),
      m.get("36 Month Beta:") flatMap (toDecimal(_)),
      m.get("Annual Dividend Rate, $:") flatMap (toDecimal(_)),
      m.get("Most Recent Dividend:") flatMap (toDecimal(_)),
      m.get("Annual Dividend Yield, %:") flatMap toPercent,
      m.get("Most Recent Split:"),
      m.get("Annual EPS, $:") flatMap (toDecimal(_)),
      m.get("Most Recent Earnings, $:") flatMap (toDecimal(_)),
      m.get("12 Month EPS Change:") flatMap (toDecimal(_)),
      m.get("Last Quarter EPS:") flatMap (toDecimal(_)),
      m.get("1 Year Return %:") flatMap toPercent,
      m.get("3 Year Return %:") flatMap toPercent,
      m.get("5 Year Return %:") flatMap toPercent,
      m.get("5 Year Revenue Growth %:") flatMap toPercent,
      m.get("5 Year Earnings Growth %:") flatMap toPercent,
      m.get("5 Year Dividend Growth %:") flatMap toPercent,
      m.get("% of Insider Shareholders:") flatMap toPercent,
      m.get("% of Institutional Shareholders:") flatMap toPercent,
      m.get("Market Capitalization, $K:") flatMap (toDecimal(_, 1.0e3)),
      m.get("P/E Ratio:") flatMap (toDecimal(_)),
      m.get("Last Quarter Net Income, $M:") flatMap (toDecimal(_, 1.0e6)),
      m.get("Last Quarter Sales, $M:") flatMap (toDecimal(_, 1.0e6)),
      m.get("Shares Outstanding, K:") flatMap (toDecimal(_, 1.0e3)) map (_.toLong),
      responseTimeMsec)
  }

  private def parseAsOfDate(doc: NodeSeq): Option[Date] = {
    // <div class="mpbox"><span class="smgrey">Key Statistics as of <span id="dtaDate">Sun, Dec 29th, 2013</span></span> .. </div>
    ((doc \\ "div") flatMap { mpbox =>
      if ((mpbox \ "@class").exists(_.text == "mpbox")) {
        (mpbox \\ "span") flatMap { smgrey =>
          if ((smgrey \ "@class").exists(_.text == "smgrey")) {
            (smgrey \\ "span") flatMap { dtaDate =>
              if ((dtaDate \ "@id").exists(_.text == "dtaDate")) Some(dtaDate.text.trim) else None
            }
          } else Seq.empty
        }
      } else Seq.empty
    }).headOption flatMap toDate
  }

  def parseCompanyName(doc: NodeSeq) = {
    // <h1 class="fl" id="symname">Berkshire Hath Hld B (BRK.B)</h1>
    ((doc \\ "h1") ||("@id", "symname")).headOption
  }

  private def parseKeyStatistics(doc: NodeSeq): Map[String, String] = {
    // <table class="mpbox"> .. </table>
    val results = (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "mpbox")) {
        // get the rows
        (table \ "tr") flatMap { row =>
          (row \ "td") map (_.text.trim) match {
            case Seq(a, b, c, d, _*) => Seq((a, b), (c, d))
            case Seq(a, b, _*) => Seq((a, b))
            case s => Seq.empty
          }
        }
      } else Seq.empty
    }
    Map(results: _*)
  }

  private def toDecimal(s: String, factor: Double = 1.0d): Option[Double] = {
    Try(s.replaceAllLiterally("?", "").replaceAllLiterally(",", "").toDouble) match {
      case Success(value) => Some(value * factor)
      case Failure(e) => None
    }
  }

  private def toDate(s: String): Option[Date] = {
    Try(new SimpleDateFormat("E, MMM dd'th', yyyy").parse(s)) match {
      case Success(date) => Some(date)
      case Failure(e) =>
        //logger.warn(s"Failed to parse date '$s'", e)
        None
    }
  }

  private def toPercent(s: String): Option[Double] = {
    Try(s.replaceAllLiterally("?", "").replaceAllLiterally("%", "").toDouble) match {
      case Success(value) => Some(value)
      case Failure(e) => None
    }
  }

  /**
   * Represents Key Statistics Information
   */
  class BCKeyStatistics(val symbol: String,
                        val name: Option[String],
                        val asOfDate: Option[Date],
                        val beta: Option[Double], // 36-Month beta
                        val dividendRateAnnual: Option[Double],
                        val dividendMostRecent: Option[Double],
                        val annualDividendYield: Option[Double],
                        val splitMostRecent: Option[String],
                        val earningsMostRecent: Option[Double],
                        val epsAnnual: Option[Double],
                        val epsChange12Month: Option[Double],
                        val epsLastQtr: Option[Double],
                        val growthPct1Year: Option[Double],
                        val growthPct3Year: Option[Double],
                        val growthPct5Year: Option[Double],
                        val revenueGrowthPct5Year: Option[Double],
                        val earningsGrowthPct5Year: Option[Double],
                        val dividendGrowthPct5Year: Option[Double],
                        val insiderShareholdersPct: Option[Double],
                        val institutionalShareholdersPct: Option[Double],
                        val marketCap: Option[Double],
                        val netIncomeLastQtr: Option[Double],
                        val peRatio: Option[Double],
                        val salesLastQtr: Option[Double],
                        val sharesOutstanding: Option[Long],
                        val responseTimeMsec: Long)

  /**
   * Node Filter
   * @author lawrence.daniels@gmail.com
   */
  implicit class NodeFilter(nodeSeq: NodeSeq) {

    /*
     *     val companyName = ((doc \\ "h1") flatMap { h1 =>
      if ((h1 \ "@id").exists(_.text == "symname")) Some(h1.text.trim) else None
    }).headOption
     */
    def ||(id: String, value: String): Seq[String] = {
      nodeSeq flatMap { n => if ((n \ id).exists(_.text == value)) Some(n.text.trim) else None }
    }

    def |>(id: String, value: String): NodeSeq = {
      nodeSeq flatMap { n => if ((n \ id).exists(_.text == value)) Some(n) else None }
    }

    def ?>(id: String, value: String) = {
      (nodeSeq \ id) flatMap { n => n.find(_.text == value) }
    }

  }

}