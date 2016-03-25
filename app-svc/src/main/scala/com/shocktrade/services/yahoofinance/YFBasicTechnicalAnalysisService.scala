package com.shocktrade.services.yahoofinance

import java.util.Date

import scala.xml.{NodeSeq, XML}

/**
 * Yahoo! Finance: Basic Technical Analysis Service
 * @author lawrence.daniels@gmail.com
 */
object YFBasicTechnicalAnalysisService extends YFWebService {
  private[this] val logger = org.slf4j.LoggerFactory.getLogger(getClass)
  private val DIV_YIELD_r = """(\d+\.\d*)""".r

  /**
   * Retrieves for a quote for the given stock symbol
   * @param symbol the given stock symbol (e.g. "AAPL")
   * @return a [[YFBasicTechnicalAnalysis quote]]
   */
  def getQuote(symbol: String): YFBasicTechnicalAnalysis = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://finance.yahoo.com/q/ta?s=$symbol+Basic+Tech.+Analysis")
    parseDocument(symbol, doc, startTime)
  }

  /**
   * Parses the document generating the data for a basic technical analysis
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): YFBasicTechnicalAnalysis = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the document
    val results = (doc \\ "table") flatMap { table =>
      if ((table \ "@id").exists(t => t.text == "table1" || t.text == "table2")) {
        (table \ "tr") map { row =>
          val header = (row \ "th") map (_.text.trim) mkString " "
          val data = (row \ "td") map (_.text.trim) mkString " "
          (header, data)
        }
      } else Seq.empty
    }

    toBasicTechnicalAnalysis(symbol, Map(results: _*), responseTimeMsec)
  }

  private def toBasicTechnicalAnalysis(symbol: String, m: Map[String, String], responseTimeMsec: Long) = {
    // extract compound values
    val (low, high) = tuple(m ~> "Day's Range:", "-")
    val (low52Week, high52Week) = tuple(m ~> "52wk Range:", "-")
    val (bid, bidSize) = tuple(m ~> "Bid:", "x")
    val (ask, askSize) = tuple(m ~> "Ask:", "x")
    val (dividend, divYield) = getDivYield(m ~> "Div & Yield:")

    // create the instance
    new YFBasicTechnicalAnalysis(
      symbol,
      m ~> "Prev Close" flatMap asNumber,
      m ~> "Open:" flatMap asNumber,
      low flatMap asNumber,
      high flatMap asNumber,
      low52Week flatMap asNumber,
      high52Week flatMap asNumber,
      bid flatMap asNumber,
      bidSize flatMap asInt,
      ask flatMap asNumber,
      askSize flatMap asInt,
      m ~> "1y Target Est:" flatMap asNumber,
      m ~> "Beta:" flatMap asNumber,
      m ~> "Next Earnings Date:" flatMap (asDate(_, "dd-MMM-yy")),
      m ~> "Volume:" flatMap asLong,
      m ~> "Avg Vol" flatMap asLong,
      m ~> "Market Cap:" flatMap asNumber,
      m ~> "P/E" flatMap asNumber,
      m ~> "EPS" flatMap asNumber,
      dividend,
      divYield,
      m ~> "splits",
      responseTimeMsec)
  }

  private def getDivYield(v: Option[String]): (Option[Double], Option[Double]) = {
    v match {
      case Some(s) =>
        DIV_YIELD_r.findAllIn(s).toSeq match {
          case Seq(a, b, _*) => (Some(a) flatMap asNumber, Some(b) flatMap asNumber)
          case x =>
            logger.warn(s"Unmatched seq $x")
            (None, None)
        }
      case None => (None, None)
    }
  }

  // pattern: "\\d+(.\\d+)?"
  // pattern: "\\d+(.\\d+)?[%]"

  /**
   * Represents basic technical analysis information
   * @author lawrence.daniels@gmail.com
   */
  class YFBasicTechnicalAnalysis(val symbol: String,
                                 val prevClose: Option[Double],
                                 val open: Option[Double],
                                 val low: Option[Double],
                                 val high: Option[Double],
                                 val low52Week: Option[Double],
                                 val high52Week: Option[Double],
                                 val bid: Option[Double],
                                 val bidSize: Option[Int],
                                 val ask: Option[Double],
                                 val askSize: Option[Int],
                                 val target1Y: Option[Double],
                                 val beta: Option[Double],
                                 val nextEarningsDate: Option[Date],
                                 val volume: Option[Long],
                                 val avgVol3m: Option[Long],
                                 val marketCap: Option[Double],
                                 val peRatio: Option[Double],
                                 val eps: Option[Double],
                                 val dividend: Option[Double],
                                 val divYield: Option[Double],
                                 val splits: Option[String],
                                 val responseTimeMsec: Long)

}