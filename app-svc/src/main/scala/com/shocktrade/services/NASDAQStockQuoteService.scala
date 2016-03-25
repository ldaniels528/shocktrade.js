package com.shocktrade.services

import java.text.SimpleDateFormat
import java.util.Date

import com.shocktrade.services.NASDAQStockQuoteService._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}

/**
 * NASDAQ Stock Quote and Summary Data Service
 * @author lawrence.daniels@gmail.com
 */
class NASDAQStockQuoteService() {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  /**
   * Retrieves for a real-time quote for the given ticker symbol
   * @param symbol the given ticker symbol (e.g. "GOOG")
   * @return a [[Future future]] of a [[NASDAQStockQuote quote]]
   */
  def getQuote(symbol: String)(implicit ec: ExecutionContext): Future[NASDAQStockQuote] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://www.nasdaq.com/symbol/$symbol")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Transforms the document into a stock quote
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): NASDAQStockQuote = {
    // capture the response time
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the page header
    val ph = parsePageHeader(doc)

    // parse the table data's key-value pairs
    val td = parseDataTable(doc)
    val (ask, bid) = numericTuple(td.get("Best Bid /Ask:"), "[/]")
    val (high, low) = numericTuple(td.get("Today's High/Low:"), "[/]")
    val (_52WeekHigh, _52WeekLow) = numericTuple(td.get("52 Week High /Low:"), "[/]")

    // create the quote instance
    new NASDAQStockQuote(
      symbol,
      parseCompanyName(doc),
      ph.get("qwidget_lastsale") flatMap asDecimal,
      ph.get("qwidget_netchange") flatMap asDecimal,
      ph.get("qwidget_percent") flatMap asDecimal,
      ph.get("qwidget_markettime"),
      ph.get("qwidget_markettime") flatMap asTradeDate,
      ask, bid,
      td.get("1 Year Target:") flatMap asDecimal,
      high, low,
      td.get("Share Volume:") flatMap asDecimal map (_.toLong),
      td ~> "50 Day Avg. Daily Volume" flatMap asDecimal map (_.toLong),
      td.get("Previous Close:") flatMap asDecimal,
      _52WeekHigh, _52WeekLow,
      td ~> "Market cap" flatMap asDecimal,
      td.get("P/E Ratio:") flatMap asDecimal,
      td ~> "Forward P/E(1y)" flatMap asDecimal,
      td ~> "Earnings Per Share (EPS)" flatMap asDecimal,
      td ~> "Annualized dividend" flatMap asDecimal,
      td ~> "Ex Dividend Date" flatMap asDate,
      td.get("Dividend Payment Date") flatMap asDate,
      td.get("Current Yield") flatMap asDecimal,
      td ~> "Beta" flatMap asDecimal,
      td ~> "NASDAQ Official Open Price" flatMap asDecimal,
      td ~> "Date of Open Price" flatMap asDate,
      td ~> "NASDAQ Official Close Price" flatMap asDecimal,
      td ~> "Date of Close Price" flatMap asDate,
      td ~> "Community Sentiment",
      parseCompanyDescription(doc),
      responseTimeMsec)
  }

  /**
   * Parses the company name of the meta tag title entity
   */
  private def parseCompanyName(doc: NodeSeq): Option[String] = {
    // gather the sequence data
    val seq = (doc \\ "meta") flatMap { meta =>
      (meta \ "@property") flatMap { m =>
        if (m.exists(_.text.trim == "og:title")) meta.attribute("content") map (_.text.trim) else None
      }
    }

    // chop-off the symbol (e.g. "FaceBook, Inc. (FB)")
    for {
      title <- seq.headOption
      index = title.lastIndexOf("(")
    } yield if (index != -1) title.substring(0, index - 1).trim else title
  }

  /**
   * Parses the company description (business summary) from the document
   */
  private def parseCompanyDescription(doc: NodeSeq): Option[String] = {
    // <div style="width:440px">
    val seq = (doc \\ "div") flatMap { div =>
      if ((div \ "@style").exists(_.text.trim == "width:440px")) {
        // <h2>&nbsp;Company Description  (as filed with the SEC)</h2> ...
        (div \ "h2") flatMap { h2 =>
          if (h2.exists(_.text.contains("Company Description"))) {
            // <p style="margin-top: 12px;">
            (div \ "p") map (_.text.trim)
          } else Seq.empty
        }
      } else Seq.empty
    }
    seq.headOption
  }

  /**
   * Parses the stock quote data from the main data table
   */
  private def parseDataTable(doc: NodeSeq): Map[String, String] = {
    // <table class="datatable1_qn"> .. </table>
    val kvps = (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "datatable1_qn")) {
        (table \ "tr") flatMap { row =>
          (row \ "td") map (n => degunk(n.text)) toList match {
            case key :: value :: Nil => Some((key, value))
            case list =>
              logger.info(s"Non-match: $list")
              None
          }
        }
      } else Seq.empty
    }

    Map[String, String](kvps: _*)
  }

  private def parsePageHeader(doc: NodeSeq): Map[String, String] = {
    val kvps = (doc \\ "div") flatMap { div =>
      val divIds = div \ "@id"
      if (divIds.exists(_.text.trim == "qwidget_quote")) {
        // parse the last sale, change, and change %
        // <div id="...">...</div>
        (div \ "div") flatMap { inner =>
          for {node <- inner.attribute("id")} yield (degunk(node.text), degunk(inner.text))
        }
      } else if (divIds.exists(_.text.trim == "qwidget_markettimedate")) {
        // parse trade date/time
        // <span id="qwidget_markettime">Jan. 27, 2014 11:01 ET</span>
        (div \ "span") flatMap { span =>
          for {node <- span.attribute("id")} yield (degunk(node.text), degunk(span.text))
        }
      } else Seq.empty
    }

    Map[String, String](kvps: _*)
  }

  private def tuple(v: Option[String], regex: String): (Option[String], Option[String]) = {
    v match {
      case Some(s) => s.split(regex) map (_.trim) match {
        case Array(a, b, _*) => (Some(a), Some(b))
        case Array(a, _*) => (Some(a), None)
        case _ => (None, None)
      }
      case None => (None, None)
    }
  }

  private def numericTuple(v: Option[String], regex: String): (Option[Double], Option[Double]) = {
    val (t1, t2) = tuple(v, regex)
    (t1 flatMap asDecimal, t2 flatMap asDecimal)
  }

  private def asDecimal(s: String): Option[Double] = {
    val t = s.replaceAllLiterally("$", "").replaceAllLiterally("%", "").replaceAllLiterally(",", "").trim
    if (t == "" || t == "N/A") None else Some(t.toDouble)
  }

  private def asTradeDate(s: String): Option[Date] = {
    def format1(s: String): Option[Date] = parseDate("MMM'.' dd, yyyy HH:mm z", s.replaceAll("ET", "-0500"))
    def format2(s: String): Option[Date] = parseDate("MMM'.' dd, yyyy", s)
    format1(s) match {
      case Some(date) => Some(date)
      case None => format2(s)
    }
  }

  private def asDate(s: String): Option[Date] = parseDate("MMM'.' dd, yyyy", s)

  private def parseDate(format: String, s: String): Option[Date] = {
    if (s == "" || s == "N/A") None
    else {
      Try(new SimpleDateFormat(format).parse(s)) match {
        case Success(date) => Some(date)
        case Failure(e) =>
          logger.error(s"Error parsing date '$s'")
          None
      }
    }
  }

  /**
   * Removes spurious (non-displayable ASCII) characters
   */
  private def degunk(s: String): String = {
    String.valueOf(s map (c => if (c < 32 || c > 127 || c == '\t') ' ' else c)).trim
  }

}

/**
 * NASDAQ Stock Quote Service Singleton
 * @author lawrence.daniels@gmail.com
 */
object NASDAQStockQuoteService {

  import java.util.Date

  private[this] lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  class NASDAQStockQuote(
                          val symbol: String,
                          val name: Option[String],
                          val lastTrade: Option[Double],
                          val change: Option[Double],
                          val changePct: Option[Double],
                          val marketTime: Option[String],
                          val tradeDateTime: Option[Date],
                          val ask: Option[Double],
                          val bid: Option[Double],
                          val target1Yr: Option[Double],
                          val high: Option[Double],
                          val low: Option[Double],
                          val volume: Option[Long],
                          val avgVol50Days: Option[Long],
                          val prevClose: Option[Double],
                          val high52Week: Option[Double],
                          val low52Week: Option[Double],
                          val marketCap: Option[Double],
                          val peRatio: Option[Double],
                          val peRatioFoward1Yr: Option[Double],
                          val eps: Option[Double],
                          val dividend: Option[Double],
                          val exDividendDate: Option[Date],
                          val dividendPaymentDate: Option[Date],
                          val divYield: Option[Double],
                          val beta: Option[Double],
                          val open: Option[Double],
                          val openDate: Option[Date],
                          val close: Option[Double],
                          val closeDate: Option[Date],
                          val communitySentiment: Option[String],
                          val businessSummary: Option[String],
                          val responseTimeMsec: Long)

  /**
   * Enriched Map
   * @author lawrence.daniels@gmail.com
   */
  implicit class EnrichedMap(m: Map[String, String]) {

    /**
     * Finds the key that starts with the prefix
     */
    def ~>(prefix: String): Option[String] = {
      m.toSeq find (_._1.startsWith(prefix)) match {
        case Some((k, v)) => cleanse(v)
        case _ =>
          logger.error(s"Failed to map '$prefix'")
          None
      }
    }

    private def cleanse(s: String) = if (s == "" || s == "N/A") None else Some(s)
  }

}
