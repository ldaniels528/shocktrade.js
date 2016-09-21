package com.shocktrade.services

import com.shocktrade.services.YahooFinanceCSVQuotesService._
import com.shocktrade.util.ParsingHelper._
import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.moment.timezone._
import org.scalajs.nodejs.request._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Yahoo Finance! CSV Quotes Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceCSVQuotesService()(implicit require: NodeRequire) {
  private val request = Request()
  private val moment = Moment()
  MomentTimezone()

  /**
    * Performs the service call and returns a single object read from the service.
    * @param symbol the given stock symbol (e.g., "AAPL")
    * @param params the given stock fields (e.g., "soxq2")
    * @return the [[YFCSVQuote quote]]
    */
  def getQuote(params: String, symbol: String)(implicit ec: ExecutionContext) = {
    val startTime = js.Date.now()
    request.getFuture(s"http://finance.yahoo.com/d/quotes.csv?s=$symbol&f=$params") map { case (response, data) =>
      parseQuote(symbol, params, data, startTime)
    }
  }

  /**
    * Performs the service call and returns a single object read from the service.
    * @param symbols the given stock symbols (e.g., "AAPL", "AMD", "INTC")
    * @param params  the given stock fields (e.g., "soxq2")
    * @return the [[YFCSVQuote quote]]
    */
  def getQuotes(params: String, symbols: Seq[String])(implicit ec: ExecutionContext) = {
    val startTime = js.Date.now()
    val symbolList = symbols mkString "+"
    request.getFuture(s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params") map { case (response, data) =>
      val lines = data.split("[\n]")
      (symbols zip lines) map { case (symbol, line) => parseQuote(symbol, params, line, startTime) }
    }
  }

  /**
    * Returns all supported parameter codes
    * @return all supported parameter codes
    */
  def getAllParams: String = FIELD_CODE_TO_MAPPING.keys.mkString

  /**
    * Returns the parameter codes required to retrieve values for the given fields
    * @return the parameter codes required to retrieve values for the given fields
    */
  def getParams(fields: String*): String = {
    (fields flatMap FIELD_CODE_TO_MAPPING.get).map(c => if (c.endsWith("0")) c.dropRight(1) else c).mkString
  }

  /**
    * Parses the given encoded stock quote string into a stock quote object
    * @param paramdata the given encoded parameter string
    * @param csvdata   the given stock quote data
    * @return the [[YFCSVQuote]]
    */
  def parseQuote(symbol: String, paramdata: String, csvdata: String, startTime: Double): YFCSVQuote = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the fields and parameter data
    val fields: Seq[String] = parseCSVData(csvdata.trim)
    val params: Seq[String] = parseParams(paramdata.toLowerCase)
    val kvps = js.Dictionary(params.zip(fields): _*)

    // transform the mapping into a bean
    toQuote(symbol, kvps, responseTimeMsec)
  }

  private def parseCSVData(command: String): Seq[String] = {
    val sb = new StringBuilder()
    var inDQ = false

    def newToken = {
      val tok = sb.toString()
      sb.clear()
      if (tok != "") Seq(tok) else Seq.empty
    }
    def toggleDQuotes = {
      inDQ = !inDQ
      Seq.empty
    }
    def append(c: Char) = {
      sb += c
      Seq.empty
    }

    val results = command flatMap {
      case '"' => toggleDQuotes
      case c if c.isSpaceChar || c == ',' => if (!inDQ) newToken else append(c)
      case c => append(c)
    }

    if (sb.isEmpty) results else results :+ sb.toString
  }

  private def parseParams(s: String): Seq[String] = {
    "([a-z][0-9])|([a-z])".r.findAllIn(s) map (s => if (s.length == 1) s + "0" else s) toSeq
  }

  private def toQuote(symbol: String, kvps: js.Dictionary[String], responseTimeMsec: Double): YFCSVQuote = {
    // convert the key-value pairs to method-value pairs
    val t = Map((kvps flatMap { case (k, v) => mapCodeToNamedValues(k, v) } flatten) toSeq: _*)
    val m = appendLastTradeDateTime(t)
    new YFCSVQuote(
      symbol,
      /* a0 */ m.uget("ask") flatMap decimalValue,
      /* a2 */ m.uget("avgVol") flatMap decimalValue,
      /* a5 */ m.uget("askSize") flatMap intValue,
      /* b0 */ m.uget("bid") flatMap decimalValue,
      /* b2 */ m.uget("askRealTime") flatMap decimalValue,
      /* b3 */ m.uget("bidRealTime") flatMap decimalValue,
      /* b4 */ m.uget("bookValuePerShare") flatMap decimalValue,
      /* b6 */ m.uget("bidSize") flatMap intValue,
      /* c1 */ m.uget("change") flatMap decimalValue,
      /* c3 */ m.uget("commission") flatMap decimalValue,
      /* c4 */ m.uget("currencyCode") flatMap stringValue,
      /* c6 */ m.uget("changeRealTime") flatMap decimalValue,
      /* c8 */ m.uget("changeAfterHours") flatMap decimalValue,
      /* d0 */ m.uget("divShare") flatMap decimalValue,
      /* d1 */ m.uget("tradeDate"),
      /* ++ */ m.uget("tradeDateTime") map (s => new js.Date(s)),
      /* e0 */ m.uget("eps") flatMap decimalValue,
      /* e1 */ m.uget("errorMessage") flatMap stringValue,
      /* e7 */ m.uget("epsEstCurrentYear") flatMap decimalValue,
      /* e8 */ m.uget("epsEstNextYear") flatMap decimalValue,
      /* e9 */ m.uget("epsEstNextQtr") flatMap decimalValue,
      /* f6 */ m.uget("floatShares") flatMap decimalValue,
      /* g0 */ m.uget("low") flatMap decimalValue,
      /* g1 */ m.uget("holdingsGainPct") flatMap decimalValue,
      /* g3 */ m.uget("annualizedGain") flatMap decimalValue,
      /* g4 */ m.uget("holdingsGain") flatMap decimalValue,
      /* g5 */ m.uget("holdingsGainPctRealTime") flatMap decimalValue,
      /* g6 */ m.uget("holdingsGainRealTime") flatMap decimalValue,
      /* h0 */ m.uget("high") flatMap decimalValue,
      /* i0 */ m.uget("moreInfo") flatMap stringValue,
      /* i5 */ m.uget("orderBookRealTime") flatMap decimalValue,
      /* j0 */ m.uget("low52Week") flatMap decimalValue,
      /* j1 */ m.uget("marketCap") flatMap decimalValue,
      /* j2 */ m.uget("sharesOutstanding") flatMap decimalValue,
      /* j3 */ m.uget("marketCapRealTime") flatMap decimalValue,
      /* j4 */ m.uget("EBITDA") flatMap decimalValue,
      /* j5 */ m.uget("change52WeekLow") flatMap decimalValue,
      /* j6 */ m.uget("changePct52WeekLow") flatMap decimalValue,
      /* k0 */ m.uget("high52Week") flatMap decimalValue,
      /* k2 */ m.uget("changePctRealTime") flatMap decimalValue,
      /* k3 */ m.uget("lastTradeSize") flatMap intValue,
      /* k4 */ m.uget("change52WeekHigh") flatMap decimalValue,
      /* k5 */ m.uget("changePct52WeekHigh") flatMap decimalValue,
      /* l1 */ m.uget("lastTrade") flatMap decimalValue,
      /* l2 */ m.uget("highLimit") flatMap decimalValue,
      /* l3 */ m.uget("lowLimit") flatMap decimalValue,
      /* m3 */ m.uget("movingAverage50Day") flatMap decimalValue,
      /* m4 */ m.uget("movingAverage200Day") flatMap decimalValue,
      /* m5 */ m.uget("change200DayMovingAvg") flatMap decimalValue,
      /* m6 */ m.uget("changePct200DayMovingAvg") flatMap decimalValue,
      /* m7 */ m.uget("change50DayMovingAvg") flatMap decimalValue,
      /* m8 */ m.uget("changePct50DayMovingAvg") flatMap decimalValue,
      /* n0 */ m.uget("name") flatMap stringValue,
      /* n4 */ m.uget("notes") flatMap stringValue,
      /* o0 */ m.uget("open") flatMap decimalValue,
      /* p0 */ m.uget("prevClose") flatMap decimalValue,
      /* p1 */ m.uget("pricePaid") flatMap decimalValue,
      /* p2 */ m.uget("changePct") flatMap decimalValue,
      /* p5 */ m.uget("priceOverSales") flatMap decimalValue,
      /* p6 */ m.uget("priceOverBook") flatMap decimalValue,
      /* q0 */ m.uget("exDividendDate") flatMap dateValue,
      /* q2 */ m.uget("close") flatMap decimalValue,
      /* r0 */ m.uget("peRatio") flatMap decimalValue,
      /* r1 */ m.uget("dividendPayDate") flatMap dateValue,
      /* r2 */ m.uget("peRatioRealTime") flatMap decimalValue,
      /* r5 */ m.uget("pegRatio") flatMap decimalValue,
      /* r6 */ m.uget("priceOverEPSCurYr") flatMap decimalValue,
      /* r7 */ m.uget("priceOverEPSNextYr") flatMap decimalValue,
      /* s0 */ m.uget("symbol") flatMap stringValue,
      /* ?? */ getChangedSymbol(m.uget("errorMessage")),
      /* s1 */ m.uget("sharesOwned") flatMap stringValue,
      /* s6 */ m.uget("revenue") flatMap decimalValue,
      /* s7 */ m.uget("shortRatio") flatMap decimalValue,
      /* t1 */ m.uget("tradeTime") flatMap stringValue,
      /* t8 */ m.uget("target1Y") flatMap decimalValue,
      /* v0 */ m.uget("volume") flatMap decimalValue,
      /* v1 */ m.uget("holdingsValue") flatMap decimalValue,
      /* v7 */ m.uget("holdingsValueRealTime") flatMap decimalValue,
      /* w1 */ m.uget("daysChange") flatMap decimalValue,
      /* w4 */ m.uget("daysChangeRealTime") flatMap decimalValue,
      /* x0 */ m.uget("exchange") map (_.toUpperCase),
      /* y0 */ m.uget("divYield") flatMap decimalValue,
      /* ++ */ responseTimeMsec)
  }

  private def mapCodeToNamedValues(code: String, data: String): Option[Seq[(String, String)]] = {
    val value = stringValue(data).toOption
    code match {
      case "a0" => value map (s => Seq("ask" -> s))
      case "a2" => value map (s => Seq("avgVol" -> s))
      case "a5" => value map (s => Seq("askSize" -> s))
      case "b0" => value map (s => Seq("bid" -> s))
      case "b2" => value map (s => Seq("askRealTime" -> s))
      case "b3" => value map (s => Seq("bidRealTime" -> s))
      case "b4" => value map (s => Seq("bookValuePerShare" -> s))
      case "b6" => value map (s => Seq("bidSize" -> s))
      case "c0" => extractChangeAndPercent(data)
      case "c1" => value map (s => Seq("change" -> s))
      case "c3" => value map (s => Seq("commission" -> s))
      case "c4" => value map (s => Seq("currencyCode" -> s))
      case "c6" => value map (s => Seq("changeRealTime" -> s))
      case "c8" => extractChangeAterHours(data)
      case "d0" => value map (s => Seq("divShare" -> s))
      case "d1" => value map (s => Seq("tradeDate" -> s))
      //case "d3" => value map (s => Seq("tradeDate" -> s))
      case "e0" => value map (s => Seq("eps" -> s))
      case "e1" => value map (s => Seq("errorMessage" -> s))
      case "e7" => value map (s => Seq("epsEstCurrentYear" -> s))
      case "e8" => value map (s => Seq("epsEstNextYear" -> s))
      case "e9" => value map (s => Seq("epsEstNextQtr" -> s))
      case "f6" => value map (s => Seq("floatShares" -> s))
      case "g0" => value map (s => Seq("low" -> s))
      case "g1" => value map (s => Seq("holdingsGainPct" -> s))
      case "g3" => value map (s => Seq("annualizedGain" -> s))
      case "g4" => value map (s => Seq("holdingsGain" -> s))
      case "g5" => value map (s => Seq("holdingsGainPctRealTime" -> s))
      case "g6" => value map (s => Seq("holdingsGainRealTime" -> s))
      case "h0" => value map (s => Seq("high" -> s))
      case "i0" => value map (s => Seq("moreInfo" -> s))
      case "i5" => value map (s => Seq("orderBookRealTime" -> s))
      case "j0" => value map (s => Seq("low52Week" -> s))
      case "j1" => value map (s => Seq("marketCap" -> s))
      case "j2" => value map (s => Seq("sharesOutstanding" -> s))
      case "j3" => value map (s => Seq("marketCapRealTime" -> s))
      case "j4" => value map (s => Seq("EBITDA" -> s))
      case "j5" => value map (s => Seq("change52WeekLow" -> s))
      case "j6" => value map (s => Seq("changePct52WeekLow" -> s))
      case "k0" => value map (s => Seq("high52Week" -> s))
      case "k1" => extractLastTradeWithTime(data)
      case "k2" => value map (s => Seq("changePctRealTime" -> s))
      case "k3" => value map (s => Seq("lastTradeSize" -> s))
      case "k4" => value map (s => Seq("change52WeekHigh" -> s))
      case "k5" => value map (s => Seq("changePct52WeekHigh" -> s))
      case "l0" => extractLastTradeWithTime(data)
      case "l1" => value map (s => Seq("lastTrade" -> s))
      case "l2" => value map (s => Seq("highLimit" -> s))
      case "l3" => value map (s => Seq("lowLimit" -> s))
      case "m0" => extract52WeekRange(data)
      case "m2" => extract52WeekRangeRealTime(data)
      case "m3" => value map (s => Seq("movingAverage50Day" -> s))
      case "m4" => value map (s => Seq("movingAverage200Day" -> s))
      case "m5" => value map (s => Seq("change200DayMovingAvg" -> s))
      case "m6" => value map (s => Seq("changePct200DayMovingAvg" -> s))
      case "m7" => value map (s => Seq("change50DayMovingAvg" -> s))
      case "m8" => value map (s => Seq("changePct50DayMovingAvg" -> s))
      case "n0" => value map (s => Seq("name" -> s))
      case "n4" => value map (s => Seq("notes" -> s))
      case "o0" => value map (s => Seq("open" -> s))
      case "p0" => value map (s => Seq("prevClose" -> s))
      case "p1" => value map (s => Seq("pricePaid" -> s))
      case "p2" => value map (s => Seq("changePct" -> s))
      case "p5" => value map (s => Seq("priceOverSales" -> s))
      case "p6" => value map (s => Seq("priceOverBook" -> s))
      case "q0" => value map (s => Seq("exDividendDate" -> s))
      case "q2" => value map (s => Seq("close" -> s))
      case "r0" => value map (s => Seq("peRatio" -> s))
      case "r1" => value map (s => Seq("dividendPayDate" -> s))
      case "r2" => value map (s => Seq("peRatioRealTime" -> s))
      case "r5" => value map (s => Seq("pegRatio" -> s))
      case "r6" => value map (s => Seq("priceOverEPSCurYr" -> s))
      case "r7" => value map (s => Seq("priceOverEPSNextYr" -> s))
      case "s0" => value map (s => Seq("symbol" -> s))
      case "s1" => value map (s => Seq("sharesOwned" -> s))
      case "s6" => value map (s => Seq("revenue" -> s))
      case "s7" => value map (s => Seq("shortRatio" -> s))
      case "t1" => value map (s => Seq("tradeTime" -> s))
      case "t8" => value map (s => Seq("target1Y" -> s))
      case "v0" => value map (s => Seq("volume" -> s))
      case "v1" => value map (s => Seq("holdingsValue" -> s))
      case "v7" => value map (s => Seq("holdingsValueRealTime" -> s))
      case "w0" => extract52WeekRange(data)
      case "w1" => value map (s => Seq("daysChange" -> s))
      case "w4" => value map (s => Seq("daysChangeRealTime" -> s))
      case "x0" => value map (s => Seq("exchange" -> s.toUpperCase))
      case "y0" => value map (s => Seq("divYield" -> s))
      case _ =>
        console.error(s"Code '$code' was not recognized (value = '$value')")
        None
    }
  }

  /**
    * Optionally extracts the new ticker from given HTML string
    */
  private def getChangedSymbol(htmlString: js.UndefOr[String]): js.UndefOr[String] = {

    // error message is: "Ticker symbol has changed to <a href="/q?s=TNCC.PK">TNCC.PK</a>"
    val result = for {
      html <- htmlString.toOption

      (p0, p1) <- html.tagContent("a")

      msg = html.substring(p0, p1).trim

      cleanmsg <- if (msg.length > 0) Some(msg) else None
    } yield cleanmsg

    result.orUndefined
  }

  /**
    * Extracts the 52-Week Range components (high52Week & low52Week)
    */
  private def extract52WeekRange(codedString: String): Option[Seq[(String, String)]] = {
    tuplize(codedString) match {
      case (Some(valueA), Some(valueB)) => Some(Seq("high52Week" -> valueA, "low52Week" -> valueB))
      case (Some(valueA), None) => Some(Seq("high52Week" -> valueA))
      case _ => None
    }
  }

  /**
    * Extracts the real-time 52-Week Range components (high52WeekRealTime & low52WeekRealTime)
    */
  private def extract52WeekRangeRealTime(codedString: String): Option[Seq[(String, String)]] = {
    tuplize(codedString) match {
      case (Some(valueA), Some(valueB)) => Some(Seq("high52WeekRealTime" -> valueA, "low52WeekRealTime" -> valueB))
      case (Some(valueA), None) => Some(Seq("high52WeekRealTime" -> valueA))
      case _ => None
    }
  }

  /**
    * Extracts the change & percent change components (change & changePct)
    */
  private def extractChangeAndPercent(codedString: String): Option[Seq[(String, String)]] = {
    tuplize(codedString) match {
      case (Some(valueA), Some(valueB)) => Some(Seq("change" -> valueA, "changePct" -> valueB))
      case (Some(valueA), None) => Some(Seq("change" -> valueA))
      case _ => None
    }
  }

  /**
    * Extracts the change after hours components (changeAfterHours & ??? TODO)
    */
  private def extractChangeAterHours(codedString: String): Option[Seq[(String, String)]] = {
    tuplize(codedString) match {
      case (Some(valueA), Some(valueB)) => Some(Seq("changeAfterHours" -> valueA))
      case _ => None
    }
  }

  /**
    * Extracts the trade time and last trade components (tradeTime & lastTrade)
    */
  private def extractLastTradeWithTime(codedString: String): Option[Seq[(String, String)]] = {
    tuplize(codedString) match {
      case (Some(valueA), Some(valueB)) => Some(Seq("tradeTime" -> valueA, "lastTrade" -> valueB))
      case _ => None
    }
  }

  private def appendLastTradeDateTime(kvps: Map[String, String]): Map[String, String] = {
    (kvps.get("tradeDate"), kvps.get("tradeTime")) match {
      case (Some(tradeDate), Some(tradeTime)) =>
        Try {
          // "9/01/2016 6:17a"
          val dateString = s"$tradeDate $tradeTime"

          // format as an ISO date string
          moment(dateString, "M/DD/YYYY h:mma").tz("America/New_York").toISOString()
        } match {
          case Success(ts) => kvps + ("tradeDateTime" -> ts)
          case Failure(e) =>
            console.error(s"Error parsing date/time string [M/DD/YYYY h:mma] (date = '$tradeDate', time = '$tradeTime')")
            kvps
        }
      case _ => kvps
    }
  }

  /**
    * Indicates whether the given string is a valid time string
    * @param s the given time string (e.g. "10:12am")
    * @return true, if the given string is a valid time string
    */
  private def isTime(s: String) = s.toUpperCase.matches("\\d{1,2}:\\d{2}(:\\d{2})?(A|P|AM|PM)")

  /**
    * Extracts a value tuple from the given encoded string
    * @param encodedString the given encoded string
    * @return a tuple of options of a string
    */
  private def tuplize(encodedString: String): (Option[String], Option[String]) = {
    stringValue(encodedString).toOption match {
      case None => (None, None)
      case Some(text) =>
        text.split(" - ") map (_.trim) match {
          case Array(a, b, _*) => (Some(a), Some(b))
          case searchText =>
            console.warn(s"Separator '$searchText' was not found in '$encodedString'")
            (Some(text), None)
        }
    }
  }

  /**
    * Parses the string value into a date value
    * @param encodedString the given string value
    * @return the [[js.Date date]] or <tt>null</tt> if a parsing error occurred
    */
  private def dateValue(encodedString: String): js.UndefOr[js.Date] = {
    stringValue(encodedString) map (s => new js.Date(s))
  }

  /**
    * Parses the encoded string value into a decimal value
    * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
    * @return the [[Double]] value or <tt>null</tt> if a parsing error occurred
    */
  private def decimalValue(encodedString: String): js.UndefOr[Double] = {
    val rawString = encodedString.replaceAll("[$]", "").replaceAll("[+]", "").replaceAll("[,]", "")
    try {
      stringValue(rawString) flatMap {
        // is it blank?
        case s if s.isBlank || NOT_APPLICABLE.contains(s) => js.undefined

        // *%, *K, *M, *B, *T
        case s if s.endsWith("%") => s.dropRight(1).toDouble
        case s if s.endsWith("K") => s.dropRight(1).toDouble * 1.0e3
        case s if s.endsWith("M") => s.dropRight(1).toDouble * 1.0e6
        case s if s.endsWith("B") => s.dropRight(1).toDouble * 1.0e9
        case s if s.endsWith("T") => s.dropRight(1).toDouble * 1.0e12

        // otherwise, just convert it
        case s => s.toDouble
      }
    } catch {
      case e: Exception =>
        console.error(s"Error parsing decimal value '$rawString' ($encodedString)", e)
        js.undefined
    }
  }

  /**
    * Parses the string value into an integer value
    * @param encodedString the given string value
    * @return the [[Int]] value or <tt>null</tt> if a parsing error occurred
    */
  private def intValue(encodedString: String): js.UndefOr[Int] = decimalValue(encodedString) map (_.toInt)

  /**
    * Extracts the display-friendly string value from the encoded string value
    * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
    * @return the string value (e.g. <tt>614.33</tt>)
    */
  private def stringValue(encodedString: String): js.UndefOr[String] = {
    // if null or contains null indicator, return null  
    if (encodedString.isBlank || NOT_APPLICABLE.contains(encodedString)) js.undefined
    else {
      // trim the value
      val value = encodedString.trim()

      // if wrapped in quotes
      if (value.startsWith("\"") && value.endsWith("\"")) stringValue(value.drop(1).dropRight(1))

      // if wrapped in bold tags
      else if (value.toLowerCase.startsWith(BOLD_START) && value.toLowerCase.endsWith(BOLD_END))
        stringValue(value.substring(BOLD_START.length(), value.length() - BOLD_END.length()))

      else value
    }
  }

}

/**
  * Yahoo Finance! CSV Quotes Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceCSVQuotesService {
  // parsing constants
  val NOT_APPLICABLE = Set("NaN", "-", "N/A", "\"N/A\"", null)
  val BOLD_START = "<b>"
  val BOLD_END = "</b>"

  /**
    * Map Extensions
    * @param m the given [[Map map]]
    */
  implicit class MapExtensions(val m: Map[String, String]) extends AnyVal {

    @inline
    def uget(key: String) = m.get(key).orUndefined

  }

  /**
    * Represents a Yahoo! Finance Stock Quote (Web Service Object)
    * @see http://www.gummy-stuff.org/Yahoo-data.htm
    * @see http://www.codeproject.com/Articles/37550/Stock-quote-and-chart-from-Yahoo-in-C
    * @see http://people.stern.nyu.edu/adamodar/New_Home_Page/data.html
    * @see http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
    * @see http://code.google.com/p/yahoo-finance-managed/wiki/CSVAPI
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class YFCSVQuote(val symbol: String,
                   val ask: js.UndefOr[Double], // a0 - Ask
                   val avgVol: js.UndefOr[Double], // a2 - Average Daily Volume
                   val askSize: js.UndefOr[Int], // a5 - Ask Size
                   val bid: js.UndefOr[Double], // b0 - Bid
                   // b1 - ???? Ask/Bid?
                   val askRealTime: js.UndefOr[Double], // b2 - Ask (Real-time)
                   val bidRealTime: js.UndefOr[Double], // b3 - Bid (Real-time)
                   val bookValuePerShare: js.UndefOr[Double], // b4 - Book Value Per Share
                   val bidSize: js.UndefOr[Int], // b6 - Bid Size
                   // c0 - Change & Percent Change
                   val change: js.UndefOr[Double], // c1 - Change
                   val commission: js.UndefOr[Double], // c3 - Commission
                   val currencyCode: js.UndefOr[String], // c4 - Currency Code
                   val changeRealTime: js.UndefOr[Double], // c6 - Change (Real-time)
                   val changeAfterHours: js.UndefOr[Double], // c8 - After Hours Change (Real-time)
                   val divShare: js.UndefOr[Double], // d0 - Dividend/Share (Trailing Annual)
                   val tradeDate: js.UndefOr[String], // d1 - Last Trade Date
                   val tradeDateTime: js.UndefOr[js.Date],
                   // d2 - Trade Date? (-)
                   // d3 - Last Trade Time? ("Feb  3", "10:56am")
                   val eps: js.UndefOr[Double], // e0 - Earnings/Share (Diluted)
                   val errorMessage: js.UndefOr[String], // e1 - Error Indication (returned for symbol changed / invalid)
                   val epsEstCurrentYear: js.UndefOr[Double], // e7 - EPS Estimate Current Year
                   val epsEstNextYear: js.UndefOr[Double], // e8 - EPS Estimate Next Year
                   val epsEstNextQtr: js.UndefOr[Double], // e9 - EPS Estimate Next Quarter
                   val floatShares: js.UndefOr[Double], // f6 - Float Shares
                   val low: js.UndefOr[Double], // g0 - Day's Low
                   val holdingsGainPct: js.UndefOr[Double], // g1 - Holdings Gain Percent
                   val annualizedGain: js.UndefOr[Double], // g3 - Annualized Gain
                   val holdingsGain: js.UndefOr[Double], // g4 - Holdings Gain
                   val holdingsGainPctRealTime: js.UndefOr[Double], // g5 - Holdings Gain Percent (Real-time)
                   val holdingsGainRealTime: js.UndefOr[Double], // g6 - Holdings Gain (Real-time)
                   val high: js.UndefOr[Double], // h0 - Day's High
                   val moreInfo: js.UndefOr[String], // i0 - More Info
                   val orderBookRealTime: js.UndefOr[Double], // i5 - Order Book (Real-time)
                   val low52Week: js.UndefOr[Double], // j0 - 52-week Low
                   val marketCap: js.UndefOr[Double], // j1 - Market Capitalization
                   val sharesOutstanding: js.UndefOr[Double], // j2 - Shares Outstanding
                   val marketCapRealTime: js.UndefOr[Double], // j3 - Market Capitalization (Real-time)
                   val EBITDA: js.UndefOr[Double], // j4 - EBITDA
                   val change52WeekLow: js.UndefOr[Double], // j5 - Change From 52-week Low
                   val changePct52WeekLow: js.UndefOr[Double], // j6 - Percent Change From 52-week Low
                   val high52Week: js.UndefOr[Double], // k0 - 52-week High
                   // k1 - Last Trade With Time (Real-time)
                   val changePctRealTime: js.UndefOr[Double], // k2 - Change Percent (Real-time)
                   val lastTradeSize: js.UndefOr[Int], // k3 - Last Trade Size
                   val change52WeekHigh: js.UndefOr[Double], // k4 - Change From 52-week High
                   val changePct52WeekHigh: js.UndefOr[Double], // k5 - Percent Change From 52-week High
                   // l0 - Last Trade (With Time)
                   val lastTrade: js.UndefOr[Double], // l1 - Last Trade (Price Only)
                   val highLimit: js.UndefOr[Double], // l2 - High Limit
                   val lowLimit: js.UndefOr[Double], // l3 - Low Limit
                   // m0 - Day's Range
                   // m2 - Day's Range (Real-time)
                   val movingAverage50Day: js.UndefOr[Double], // m3 - 50-day Moving Average
                   val movingAverage200Day: js.UndefOr[Double], // m4 - 200-day Moving Average
                   val change200DayMovingAvg: js.UndefOr[Double], //m5 - Change From 200-day Moving Average
                   val changePct200DayMovingAvg: js.UndefOr[Double], // m6 - Percent Change From 200-day Moving Average
                   val change50DayMovingAvg: js.UndefOr[Double], // m7 - Change From 50-day Moving Average
                   val changePct50DayMovingAvg: js.UndefOr[Double], // m8 - Percent Change From 50-day Moving Average
                   val name: js.UndefOr[String], // n0 - Name
                   val notes: js.UndefOr[String], // n4 - Notes
                   val open: js.UndefOr[Double], // o0 - Open
                   val prevClose: js.UndefOr[Double], // p0 - Previous Close
                   val pricePaid: js.UndefOr[Double], // p1 - Price Paid
                   val changePct: js.UndefOr[Double], // p2 - Change in Percent
                   val priceOverSales: js.UndefOr[Double], // p5 - Price/Sales
                   val priceOverBook: js.UndefOr[Double], // p6 - Price/Book
                   val exDividendDate: js.UndefOr[js.Date], // q0 - Ex-Dividend Date
                   // q1 - ????
                   val close: js.UndefOr[Double], // q2 - Close
                   val peRatio: js.UndefOr[Double], // r0 - P/E Ratio
                   val dividendPayDate: js.UndefOr[js.Date], // r1 - Dividend Pay Date
                   val peRatioRealTime: js.UndefOr[Double], // r2 - P/E Ratio (Real-time)
                   val pegRatio: js.UndefOr[Double], // r5 - PEG Ratio (Price Earnings Growth)
                   val priceOverEPSCurYr: js.UndefOr[Double], // r6 - Price/EPS Estimate Current Year
                   val priceOverEPSNextYr: js.UndefOr[Double], // r7 - Price/EPS Estimate Next Year
                   val oldSymbol: js.UndefOr[String], // s0 - Symbol
                   val newSymbol: js.UndefOr[String], // (Derived from e1: error message)
                   val sharesOwned: js.UndefOr[String], // s1 - Shares Owned
                   val revenue: js.UndefOr[Double], // s6 - Revenue
                   val shortRatio: js.UndefOr[Double], // s7 - Short Ratio
                   val tradeTime: js.UndefOr[String], // t1 - Last Trade Time
                   // t6 - Trade Links
                   // t7 - Ticker Trend
                   val target1Yr: js.UndefOr[Double], // t8 - 1-Year Target Price
                   val volume: js.UndefOr[Double], // v0 - Volume
                   val holdingsValue: js.UndefOr[Double], // v1 - Holdings Value
                   val holdingsValueRealTime: js.UndefOr[Double], // v7 - Holdings Value (Real-time)
                   // w0 - 52-week Range
                   val daysChange: js.UndefOr[Double], // w1 - Day's Value Change
                   val daysChangeRealTime: js.UndefOr[Double], // w4 - Day's Value Change (Real-time)
                   val exchange: js.UndefOr[String], // x0 - Stock Exchange
                   val divYield: js.UndefOr[Double], // y0 - Dividend Yield (Trailing Annual)
                   val responseTimeMsec: Double) extends js.Object

  val CODE_TO_FIELD_MAPPING = Map(
    "a0" -> "ask",
    "a2" -> "avgVol",
    "a5" -> "askSize",
    "b0" -> "bid",
    // b1 - ???? Ask/Bid?
    "b2" -> "askRealTime",
    "b3" -> "bidRealTime",
    "b4" -> "bookValuePerShare",
    "b6" -> "bidSize",
    "c0" -> "changePct", // Change & Percent Change
    "c1" -> "change",
    "c3" -> "commission",
    "c4" -> "currencyCode", // Currency Code (e.g. "USD")
    "c6" -> "changeRealTime",
    "c8" -> "changeAfterHours",
    "d0" -> "divShare", // Dividend/Share (Trailing Annual)
    "d1" -> "tradeDate", // Last Trade Date
    // d2 - Trade Date? (-)
    // d3 - Last Trade Time? ("Feb  3", "10:56am")
    "e0" -> "eps", // Earnings/Share (Diluted)
    "e1" -> "errorMessage", // Error Indication (returned for symbol changed / invalid)
    "e7" -> "epsEstCurrentYear", // EPS Estimate Current Year
    "e8" -> "epsEstNextYear", // EPS Estimate Next Year
    "e9" -> "epsEstNextQtr", // EPS Estimate Next Quarter
    "f6" -> "floatShares",
    "g0" -> "low", // Day's Low
    "g1" -> "holdingsGainPct",
    "g3" -> "annualizedGain",
    "g4" -> "holdingsGain",
    "g5" -> "holdingsGainPctRealTime",
    "g6" -> "holdingsGainRealTime",
    "h0" -> "high", // Day's High
    "i0" -> "moreInfo",
    "i5" -> "orderBookRealTime",
    "j0" -> "low52Week",
    "j1" -> "marketCap",
    "j2" -> "sharesOutstanding",
    "j3" -> "marketCapRealTime",
    "j4" -> "EBITDA",
    "j5" -> "change52WeekLow",
    "j6" -> "changePct52WeekLow",
    "k0" -> "high52Week",
    // k1 - Last Trade With Time (Real-time)
    "k2" -> "changePctRealTime",
    "k3" -> "lastTradeSize",
    "k4" -> "change52WeekHigh",
    "k5" -> "changePct52WeekHigh",
    // l0 - Last Trade (With Time)
    "l1" -> "lastTrade", // Last Trade (Price Only)
    "l2" -> "highLimit",
    "l3" -> "lowLimit",
    "m0" -> "daysRange",
    // m2 - Day's Range (Real-time)
    "m3" -> "movingAverage50Day", // 50-day Moving Average
    "m4" -> "movingAverage200Day", // 200-day Moving Average
    "m5" -> "change200DayMovingAvg", // Change From 200-day Moving Average
    "m6" -> "changePct200DayMovingAvg",
    "m7" -> "change50DayMovingAvg", // Change From 50-day Moving Average
    "m8" -> "changePct50DayMovingAvg",
    "n0" -> "name",
    "n4" -> "notes",
    "o0" -> "open",
    "p0" -> "prevClose",
    "p1" -> "pricePaid",
    "p2" -> "changePct",
    "p5" -> "priceOverSales", // Price/Sales
    "p6" -> "priceOverBook", // Price/Book
    "q0" -> "exDividendDate",
    // q1 - ????
    "q2" -> "close",
    "r0" -> "peRatio", // P/E Ratio
    "r1" -> "dividendPayDate",
    "r2" -> "peRatioRealTime",
    "r5" -> "pegRatio", //  Price/Earnings Growth Ratio
    "r6" -> "priceOverEPSCurYr", // Price/EPS Estimate Current Year
    "r7" -> "priceOverEPSNextYr", //  - Price/EPS Estimate Next Year
    "s0" -> "symbol",
    "s1" -> "sharesOwned",
    "s6" -> "revenue",
    "s7" -> "shortRatio",
    "t1" -> "tradeTime", // Last Trade Time (hh:mm)
    // t6 - Trade Links
    // t7 - Ticker Trend
    "t8" -> "target1Y", //  1-Year Target Price
    "v0" -> "volume", // Volume
    "v1" -> "holdingsValue", //  Holdings Value
    "v7" -> "holdingsValueRealTime",
    // w0 - 52-week Range
    // w1 - Day's Value Change
    // w4 - Day's Value Change (Real-time)
    "x0" -> "exchange",
    "y0" -> "divYield") // Dividend Yield (Trailing Annual)

  val FIELD_CODE_TO_MAPPING = CODE_TO_FIELD_MAPPING map { case (k, v) => (v, k) }

}