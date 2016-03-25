package com.shocktrade.services

import java.util.{Calendar, Date}

import com.shocktrade.services.util.DateUtil
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}

/**
 * NASDAQ Last Sale Intra-Day Quotes Service
 * @author lawrence.daniels@gmail.com
 */
object NASDAQIntraDayQuotesService extends Throttle[Int] {
  protected lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  /**
   * Retrieves a single page of intra-day quotes for the given symbol
   * @param symbol the given symbol (e.g. "AAPL")
   * @param time the [[TimeSlot time slot]] that desired transaction time occurred within
   * @param pageNo the given page number of the results
   * @return a [[Future future]] of a sequence of [[NASDAQIntraDayQuote quotes]]
   */
  def getQuotes(symbol: String, time: TimeSlot, pageNo: Int = 1)(implicit ec: ExecutionContext): List[NASDAQIntraDayQuote] = {
    val (_, quotes) = getQuotesWithDocument(symbol, time, pageNo)
    quotes
  }

  /**
   * Retrieves all pages of intra-day quotes for the given symbol
   * @param symbol the given symbol (e.g. "AAPL")
   * @param time the [[TimeSlot time slot]] that desired transaction time occurred within
   * @return a [[Future future]] of a sequence of [[NASDAQIntraDayQuote quotes]]
   */
  def getQuotesAllPages(symbol: String, time: TimeSlot)(implicit ec: ExecutionContext): List[NASDAQIntraDayQuote] = {
    val lcSymbol = symbol.toLowerCase

    // get the initial document and transform it into quotes
    val (doc0, quotes0) = getQuotesWithDocument(lcSymbol, time, 1)

    // read the other pages
    val quotes1 = getLastPageNumber(doc0) match {
      case Some(lastPage) =>
        var list = List[NASDAQIntraDayQuote]()
        val it = (2 to lastPage).iterator
        while (it.hasNext) list = getQuotes(symbol, time, getNextItem(it)) ::: list
        list
      case None => Nil
    }

    // combine & sort all of the quotes
    (quotes0 ::: quotes1).sortBy(_.tradeDateTime)
  }

  /**
   * Retrieves a single page of intra-day quotes for the given symbol
   * @param symbol the given symbol (e.g. "AAPL")
   * @param time the [[TimeSlot time slot]] that desired transaction time occurred within
   * @param pageNo the given page number of the results
   * @return a [[Future future]] of a tuple of a [[NodeSeq]] and a sequence of [[NASDAQIntraDayQuote quotes]]
   */
  private def getQuotesWithDocument(symbol: String, time: TimeSlot, pageNo: Int)(implicit ec: ExecutionContext): (NodeSeq, List[NASDAQIntraDayQuote]) = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://www.nasdaq.com/symbol/$symbol/time-sales?time=${time.index}&pageNo=$pageNo")

    (doc, parseDocument(symbol, doc, startTime))
  }

  /**
   * Retrieves intra-day quotes for the given symbol and date range
   * @param symbol the given symbol (e.g. "AAPL")
   * @param start the start time of the date range
   * @param end the end time of the date range
   * @return a [[Future future]] of a sequence of [[NASDAQIntraDayQuote quotes]]
   */
  def getQuotesInRange(symbol: String, start: TimeSlot, end: TimeSlot)(implicit ec: ExecutionContext): Seq[NASDAQIntraDayQuote] = {
    (start.index to end.index) flatMap (t => getQuotesAllPages(symbol, getTimeSlot(t))) sortBy (_.tradeDateTime)
  }

  /**
   * Transforms the given node sequences to Option objects
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): List[NASDAQIntraDayQuote] = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // extract the date information
    val headerDate = parseHeaderDate(doc)

    // collect the date for this page
    (doc \\ "table") flatMap { table =>
      if ((table \ "@class").exists(_.text == "AfterHoursPagingContents")) {
        // extract the rows
        val rows = table \ "tr"
        if (rows.length > 1) {
          // get the header names
          val headerCols = (rows.head \ "th") map (_.text) map degunk

          // get the data mapping
          rows.tail map { row =>
            val dataCols = (row \ "td") map (_.text) map degunk
            val mappings = Map(headerCols zip dataCols: _*)
            toQuote(symbol, headerDate, mappings, responseTimeMsec)
          }
        } else Nil
      } else Nil
    } toList
  }

  private def getLastPageNumber(doc: NodeSeq): Option[Int] = {
    val ANCHOR_HEADER = "pageno="

    // attempt to retrieve the href text
    // href="http://www.nasdaq.com/symbol/amd/time-sales?time=1&pageno=245"
    val href = ((doc \\ "a") flatMap { a =>
      if ((a \ "@id").exists(_.text == "quotes_content_left_lb_LastPage")) {
        a.attribute("href") map (_.text)
      } else None
    }).headOption
    logger.info(s"href = $href")

    // now parse out just the last page number
    val pageNo = for {
      text <- href
      pageNo <- text.indexOf(ANCHOR_HEADER) match {
        case n if n != -1 => Some(text.substring(n + ANCHOR_HEADER.length))
        case _ => None
      }
    } yield pageNo

    pageNo map (_.toInt)
  }

  private def parseHeaderDate(doc: NodeSeq): Option[String] = {
    // extract the date from span#qwidget_markettime 
    ((doc \\ "span") flatMap { span =>
      if ((span \ "@id").exists(_.text == "qwidget_markettime")) Some(degunk(span.text)) else None
    }).headOption
  }

  private def parseDateTime(headerDate: Option[String], nlsTime: Option[String]): Option[Date] = {
    import Calendar._

    // get the year, month and day
    val cal = Calendar.getInstance()
    cal.setTime(DateUtil.getLastTradeStartTime())
    val (year, month, day) = (cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH))

    Try(nlsTime map (s => s + " -05:00") map HH_MM_SS_Z.parseDateTime) match {
      case Success(date) => date map (_.withDate(year, month + 1, day).toDate)
      case Failure(e) =>
        logger.warn(s"Error parsing date: time ='$nlsTime', ${e.getMessage}")
        None
    }
  }

  private def toQuote(symbol: String, headerDate: Option[String], m: Map[String, String], responseTimeMsec: Long) = {
    val nlsTime = m.get("NLS Time (ET)")
    NASDAQIntraDayQuote(
      symbol,
      parseDateTime(headerDate, nlsTime),
      headerDate,
      nlsTime,
      m.get("NLS Price") map (decimal(_).toDouble),
      m.get("NLS Share Volume") map (cleanse(_).toLong),
      responseTimeMsec)
  }

  private def decimal(s: String) = DECIMAL_r.findFirstIn(cleanse(s)) getOrElse "0"

  private def cleanse(s: String) = s.replaceAll("[,]", "")

  /**
   * Removes spurious (non-displayable ASCII) characters
   */
  private def degunk(s: String) = String.valueOf(s map (c => if (c < 32 || c > 127) ' ' else c)).trim

  private val DECIMAL_r = "(\\d+\\.\\d*)".r
  private val MMM_DD_YYYY_HH_MM_Z = DateTimeFormat.forPattern("MMM'.' dd, yyyy HH:mm:ss Z")
  private val HH_MM_SS_Z = DateTimeFormat.forPattern("HH:mm:ss Z")

  // time slot constants
  private val mappings = Seq(
    "ET_0930_TO_0959", "ET_1000_TO_1029", "ET_1030_TO_1059", "ET_1100_TO_1129",
    "ET_1130_TO_1159", "ET_1200_TO_1229", "ET_1230_TO_1259", "ET_1300_TO_1329",
    "ET_1330_TO_1359", "ET_1400_TO_1429", "ET_1430_TO_1459", "ET_1500_TO_1529",
    "ET_1530_TO_1600"
  )
  val ET_0930_TO_0959 = new TimeSlot(1)
  val ET_1000_TO_1029 = new TimeSlot(2)
  val ET_1030_TO_1059 = new TimeSlot(3)
  val ET_1100_TO_1129 = new TimeSlot(4)
  val ET_1130_TO_1159 = new TimeSlot(5)
  val ET_1200_TO_1229 = new TimeSlot(6)
  val ET_1230_TO_1259 = new TimeSlot(7)
  val ET_1300_TO_1329 = new TimeSlot(8)
  val ET_1330_TO_1359 = new TimeSlot(9)
  val ET_1400_TO_1429 = new TimeSlot(10)
  val ET_1430_TO_1459 = new TimeSlot(11)
  val ET_1500_TO_1529 = new TimeSlot(12)
  val ET_1530_TO_1600 = new TimeSlot(13)

  /**
   * Converts the integer index into a time slot instance
   */
  def getTimeSlot(index: Int): TimeSlot = {
    if (index <= 1) ET_0930_TO_0959
    else if (index >= 13) ET_1530_TO_1600
    else new TimeSlot(index)
  }

  /**
   * Returns the index that corresponds to the given time
   */
  def getTimeSlot(time: Date): TimeSlot = {
    // get the time instance for ET (GMT-05:00)
    val tzET = DateTimeZone.forID("America/New_York")
    val timeET = new DateTime(time, tzET)

    // get midnight ET time
    val midnight = timeET.toDateMidnight

    // if the time occurred after midnight ...
    if (timeET.isAfter(midnight)) {
      // compute the time index
      val hour = timeET.hourOfDay().get()
      val mins = timeET.minuteOfHour().get()
      hour match {
        case n if n <= 9 => ET_0930_TO_0959
        case 10 => if (mins < 30) ET_1000_TO_1029 else ET_1030_TO_1059
        case 11 => if (mins < 30) ET_1100_TO_1129 else ET_1130_TO_1159
        case 12 => if (mins < 30) ET_1200_TO_1229 else ET_1230_TO_1259
        case 13 => if (mins < 30) ET_1300_TO_1329 else ET_1330_TO_1359
        case 14 => if (mins < 30) ET_1400_TO_1429 else ET_1430_TO_1459
        case 15 => if (mins < 30) ET_1500_TO_1529 else ET_1530_TO_1600
        case n if n > 15 => ET_1530_TO_1600
      }
    } else ET_0930_TO_0959
  }

  /**
   * Represents an intra-day quote
   */
  case class NASDAQIntraDayQuote(symbol: String,
                                 tradeDateTime: Option[Date],
                                 date: Option[String],
                                 time: Option[String],
                                 price: Option[Double],
                                 volume: Option[Long],
                                 responseTimeMsec: Long)

  /**
   * Time Slot value class
   */
  class TimeSlot(val index: Int) extends AnyVal {

    override def toString = mappings(index - 1)

  }

}
