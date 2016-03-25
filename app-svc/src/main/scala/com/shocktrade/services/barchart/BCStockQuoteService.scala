package com.shocktrade.services.barchart

import java.text.SimpleDateFormat
import java.util.Date

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.xml.{NodeSeq, XML}

/**
 * BarChart.com Stock Quote Service
 * @see http://www.barchart.com/quotes/stocks/BRK.B
 * @author lawrence.daniels@gmail.com
 */
object BCStockQuoteService {
  private val DECIMAL_r = """-?[0-9]\d*(.\d+)""".r
  private[this] lazy val logger = LoggerFactory.getLogger(getClass)

  /**
   * Retrieves a quote for the given ticker symbol
   * @param symbol the given ticker symbol
   */
  def getQuote(symbol: String)(implicit ec: ExecutionContext): BCStockQuote = {
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
   * Transforms the document into a stock quote object
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long) = {
    // capture the response time
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // extract the header information
    val companyName = extractById(doc \\ "h1", "symname").headOption
    val spans = doc \\ "span"
    val dtaLast = extractById(spans, "dtaLast").headOption map (_.toDouble)
    val dtaChange = extractById(spans, "dtaChange").headOption map (s => DECIMAL_r.findAllIn(s).toSeq map (_.toDouble))
    val (change, changePct) = parseChangeAndChangePct(dtaChange)
    val dtaDate = extractById(spans, "dtaDate")

    // find the main-content table
    (doc \\ "table") map { mainTbl =>
      if ((mainTbl \ "@id").exists(_.text == "main-content")) {
        logger.info(s"mainTbl = $mainTbl")

        // get the rows, skip the first one...
        val rows = mainTbl \ "tr" \ "td" \ "div" \ "table" \ "tr" \ "td" \ "table" \ "tr"
        rows.tail map { row =>
          logger.info(s"row = $row")

        }
      }
    }

    // create the stock quote
    BCStockQuote(
      symbol,
      dtaLast,
      parseLastTradeTime(dtaDate),
      change,
      changePct,
      responseTimeMsec)
  }

  private def parseLastTradeTime(seq: Seq[String]): Option[Date] = {
    seq match {
      case x@Seq(d0, d1, _*) =>

        "\\d{1,2}:\\d{2}(\\S)|(\\S+)".r.findAllIn(d0).toSeq match {
          case Seq(t0, t1, _*) =>
          case aSeq => aSeq.mkString(" ")
        }

        val date = x.reverse.mkString(" ")
        Some(new SimpleDateFormat("E, MMM dd'th', yyyy hh:mma z").parse(date))
      case _ => None
    }
  }

  private def parseChangeAndChangePct(dtaChange: Option[Seq[Double]]) = {
    dtaChange match {
      case Some(values) => values match {
        case Seq(a, b, _*) => (Some(a), Some(b))
        case Seq(a, _*) => (Some(a), None)
        case _ => (None, None)
      }
      case None => (None, None)
    }
  }

  /**
   * Extracts text from a node by ID
   */
  private def extractById(nodeSeq: NodeSeq, id: String): Seq[String] = {
    nodeSeq flatMap { node =>
      if ((node \ "@id").exists(_.text == id)) Some(node.text.trim) else None
    }
  }

  /**
   * Represents a BarChart.com Stock Quote
   */
  case class BCStockQuote(
                           symbol: String,
                           lastTrade: Option[Double],
                           lastTradeTime: Option[Date],
                           change: Option[Double],
                           changePct: Option[Double],
                           responseTimeMsec: Long)

}