package com.shocktrade.server.services

import com.shocktrade.server.services.NASDAQIntraDayQuotesService._
import io.scalajs.nodejs.{Error, console}
import io.scalajs.npm.htmlparser2
import io.scalajs.npm.htmlparser2.{ParserHandler, ParserOptions}
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.moment.timezone._
import io.scalajs.npm.request.Request
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.runtime._
import scala.util.{Failure, Success, Try}

/**
  * NASDAQ Intra-day Quotes Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NASDAQIntraDayQuotesService() {
  private val timeSlotStrings = Seq(
    "ET_0930_TO_0959", "ET_1000_TO_1029", "ET_1030_TO_1059", "ET_1100_TO_1129",
    "ET_1130_TO_1159", "ET_1200_TO_1229", "ET_1230_TO_1259", "ET_1300_TO_1329",
    "ET_1330_TO_1359", "ET_1400_TO_1429", "ET_1430_TO_1459", "ET_1500_TO_1529",
    "ET_1530_TO_1600"
  )

  // load the modules
  MomentTimezone

  /**
    * Returns a response containing quotes for the given symbol starting from the given time slot and page number
    * @param symbol         the given symbol (e.g. "INTC")
    * @param timeSlot       the given [[TimeSlot time slot]]
    * @param startingPageNo the optional starting page number
    * @param ec             the implicit execution context
    * @return a response containing quotes for the given symbol
    */
  def apply(symbol: String, timeSlot: TimeSlot, startingPageNo: Int = 1)(implicit ec: ExecutionContext): Future[NASDAQIntraDayResponse] = {
    collectPages(getPage(toURL(symbol, timeSlot, startingPageNo))) map (pages => new NASDAQIntraDayResponse(symbol, js.Array(pages: _*)))
  }

  /**
    * Returns a response containing quotes for the given URL
    * @param url the given URL
    * @param ec  the implicit execution context
    * @return a response containing quotes for the given symbol
    */
  private def getPage(url: String)(implicit ec: ExecutionContext) = {
    val startTime = js.Date.now()
    val promise = Promise[NASDAQIntraDayPage]()
    Request.getFuture(url) foreach { case (response, html) =>
      val parser = new htmlparser2.Parser(new ParserHandler {
        val sb = new StringBuilder()
        val headers = js.Array[String]()
        val rows = js.Array[js.Dictionary[String]]()
        var columns = js.Dictionary[String]()
        var inDiv: Boolean = false
        var inTable: Boolean = false
        var nextPageUrl_? : Option[String] = None

        override def onopentag(tag: String, attributes: js.Dictionary[String]) {
          tag match {
            case "a" if attributes.get("id").contains(s"quotes_content_left_lb_NextPage") => nextPageUrl_? = attributes.get("href")
            case "div" if !inDiv && attributes.get("id").contains("quotes_content_left__panelTradeData") => inDiv = true
            case "table" if inDiv && !inTable => inTable = true
            case _ =>
          }
        }

        override def onclosetag(tag: String) {
          tag match {
            case "table" if inDiv && inTable =>
              inTable = false
              inDiv = false
            case "td" if inDiv && inTable =>
              columns(headers(columns.size)) = sb.toString().trim
            case "th" if inDiv && inTable =>
              headers.append(sb.toString().trim)
            case "tr" if inDiv && inTable =>
              rows.append(columns)
              columns = js.Dictionary[String]()
            case _ =>
          }
          sb.clear()
        }

        override def ontext(text: String): Unit = sb.append(text)

        override def onend(): Unit = promise.success(toPage(headers, rows, url, nextPageUrl_?, responseTime = js.Date.now() - startTime))

        override def onerror(err: Error): Unit = promise.failure(wrapJavaScriptException(err))

      }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

      parser.write(html)
      parser.end()
    }
    promise.future
  }

  /**
    * Returns the appropriate time slot constant based on the given time
    * @param time the given time
    * @return the [[TimeSlot time slot]]
    */
  @inline
  def getTimeSlot(time: js.Date): TimeSlot = {
    Moment(time).tz("America/New_York").format("HHmm").toInt match {
      case t if t < 1000 => ET_0930_TO_0959
      case t if t >= 1000 && t <= 1029 => ET_1000_TO_1029
      case t if t >= 1030 && t <= 1059 => ET_1030_TO_1059
      case t if t >= 1100 && t <= 1129 => ET_1100_TO_1129
      case t if t >= 1130 && t <= 1159 => ET_1130_TO_1159
      case t if t >= 1200 && t <= 1229 => ET_1200_TO_1229
      case t if t >= 1230 && t <= 1259 => ET_1230_TO_1259
      case t if t >= 1300 && t <= 1329 => ET_1300_TO_1329
      case t if t >= 1330 && t <= 1359 => ET_1330_TO_1359
      case t if t >= 1400 && t <= 1429 => ET_1400_TO_1429
      case t if t >= 1430 && t <= 1459 => ET_1430_TO_1459
      case t if t >= 1500 && t <= 1529 => ET_1500_TO_1529
      case t if t >= 1530 => ET_1530_TO_1600
    }
  }

  /**
    * Returns the text-based identifier for the given time slot
    * @param timeSlot the given [[TimeSlot time slot]]
    * @return the text-based identifier (e.g. "ET_0930_TO_0959")
    */
  @inline
  def getTimeSlotText(timeSlot: TimeSlot): String = timeSlotStrings(timeSlot - 1)

  /**
    * Recursively retrieves the next page of results for each response and returns a composite response reflecting the results
    * @param promisedPage the promise of a [[NASDAQIntraDayPage page]]
    * @param ec           the implicit execution context
    * @return a composite response reflecting the results
    */
  @inline
  private def collectPages(promisedPage: Future[NASDAQIntraDayPage])(implicit ec: ExecutionContext): Future[List[NASDAQIntraDayPage]] = {
    for {
      page0 <- promisedPage
      pages <- page0.nextPageUrl.flat.toOption match {
        case Some(nextPageUrl) => collectPages(getPage(nextPageUrl)) map { list => page0 :: list }
        case None => promisedPage map (List(_))
      }
    } yield pages
  }

  @inline
  private def toPage(headers: js.Array[String],
                     rows: js.Array[js.Dictionary[String]],
                     pageUrl: String,
                     nextPageUrl_? : Option[String],
                     responseTime: Double) = {
    new NASDAQIntraDayPage(
      pageUrl = pageUrl,
      nextPageUrl = nextPageUrl_?.orUndefined,
      responseTime = responseTime,
      quotes = (if (rows.nonEmpty) rows.tail else emptyArray) map { row =>
        new NASDAQIntraDayQuote(
          price = row.get("NLS Price").orUndefined flatMap toNumber,
          time = row.get("NLS Time (ET)") orUndefined,
          volume = row.get("NLS Share Volume").orUndefined flatMap toNumber
        )
      })
  }

  /**
    * Generates the URL for the service call
    * @param symbol   the given symbol (e.g. "INTC")
    * @param timeSlot the given [[TimeSlot time slot]]
    * @param pageNo   the given page number
    * @return the URL for the service call (e.g. "http://www.nasdaq.com/symbol/aapl/time-sales?time=2")
    */
  @inline
  private def toURL(symbol: String, timeSlot: TimeSlot, pageNo: Int) = {
    s"http://www.nasdaq.com/symbol/${symbol.toLowerCase}/time-sales?time=$timeSlot&pageno=$pageNo"
  }

  @inline
  private def toNumber(s: String): js.UndefOr[Double] = {
    Try(s.filter(c => c.isDigit || c == '.').toDouble) match {
      case Success(value) => value
      case Failure(e) => console.error(e.getMessage); js.undefined
    }
  }

}

/**
  * NASDAQ Intra-day Quotes Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NASDAQIntraDayQuotesService {
  type TimeSlot = Int
  val ET_0930_TO_0959: TimeSlot = 1
  val ET_1000_TO_1029: TimeSlot = 2
  val ET_1030_TO_1059: TimeSlot = 3
  val ET_1100_TO_1129: TimeSlot = 4
  val ET_1130_TO_1159: TimeSlot = 5
  val ET_1200_TO_1229: TimeSlot = 6
  val ET_1230_TO_1259: TimeSlot = 7
  val ET_1300_TO_1329: TimeSlot = 8
  val ET_1330_TO_1359: TimeSlot = 9
  val ET_1400_TO_1429: TimeSlot = 10
  val ET_1430_TO_1459: TimeSlot = 11
  val ET_1500_TO_1529: TimeSlot = 12
  val ET_1530_TO_1600: TimeSlot = 13

  /**
    * NASDAQ Intra-day Quotes Service Response
    * @param symbol the given stock ticker
    * @param pages  the collection of pages
    */
  @ScalaJSDefined
  class NASDAQIntraDayResponse(val symbol: String,
                               val pages: js.Array[NASDAQIntraDayPage]) extends js.Object

  /**
    * NASDAQ Intra-day Quotes Service Page
    * @param pageUrl      the given URL for this page
    * @param nextPageUrl  the optional URL for the next page of results
    * @param quotes       the collection of quotes
    * @param responseTime the given mapping of URL to response time in milliseconds
    */
  @ScalaJSDefined
  class NASDAQIntraDayPage(val pageUrl: String,
                           val nextPageUrl: js.UndefOr[String],
                           val quotes: js.Array[NASDAQIntraDayQuote],
                           val responseTime: Double) extends js.Object

  /**
    * NASDAQ Intra-day Quote Snapshot
    * @param price  the given share price
    * @param time   the given trading time
    * @param volume the given trading volume
    */
  @ScalaJSDefined
  class NASDAQIntraDayQuote(val price: js.UndefOr[Double],
                            val time: js.UndefOr[String],
                            val volume: js.UndefOr[Double]) extends js.Object

}