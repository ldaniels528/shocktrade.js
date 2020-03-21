package com.shocktrade.server.services.yahoo

import com.shocktrade.common.util.ParsingHelper._
import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesParser._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService._
import io.scalajs.nodejs.console
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.moment.timezone._


import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success, Try}

/**
 * Yahoo Finance! CSV Quotes Parser
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class YahooFinanceCSVQuotesParser() {
  MomentTimezone

  /**
   * Parses the given encoded stock quote string into a stock quote object
   * @param parameters the given encoded parameter string
   * @param csvdata    the given stock quote data
   * @return the [[YFCSVQuote quote]]
   */
  def parseQuote(parameters: String, csvdata: String, startTime: Double): Option[YFCSVQuote] = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the fields and parameter data
    val fields = parseCSVData(csvdata.trim)
    val params = parseParams(parameters.toLowerCase)
    val kvps = js.Dictionary(params zip fields: _*)

    // transform the mapping into a bean
    toQuote(kvps, responseTimeMsec)
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

  private def toQuote(kvps: js.Dictionary[String], responseTimeMsec: Double) = {
    // convert the key-value pairs to method-value pairs
    val t = Map((kvps flatMap { case (k, v) => mapCodeToNamedValues(k, v) } flatten) toSeq: _*)
    val m = appendLastTradeDateTime(t)
    m.get("symbol") map { symbol =>
      new YFCSVQuote(
        symbol = symbol,
        /* a0 */ ask = m.getDecimal("ask"),
        /* a2 */ avgVol = m.getDecimal("avgVol"),
        /* a5 */ askSize = m.getInteger("askSize"),
        /* b0 */ bid = m.getDecimal("bid"),
        /* b2 */ askRealTime = m.getDecimal("askRealTime"),
        /* b3 */ bidRealTime = m.getDecimal("bidRealTime"),
        /* b4 */ bookValuePerShare = m.getDecimal("bookValuePerShare"),
        /* b6 */ bidSize = m.getInteger("bidSize"),
        /* c1 */ change = m.getDecimal("change"),
        /* c3 */ commission = m.getDecimal("commission"),
        /* c4 */ currencyCode = m.getString("currencyCode"),
        /* c6 */ changeRealTime = m.getDecimal("changeRealTime"),
        /* c8 */ changeAfterHours = m.getDecimal("changeAfterHours"),
        /* d0 */ divShare = m.getDecimal("divShare"),
        /* d1 */ tradeDate = m.uget("tradeDate"),
        /* ++ */ tradeDateTime = m.uget("tradeDateTime") map (s => new js.Date(s)),
        /* e0 */ eps = m.getDecimal("eps"),
        /* e1 */ errorMessage = m.getString("errorMessage"),
        /* e7 */ epsEstCurrentYear = m.getDecimal("epsEstCurrentYear"),
        /* e8 */ epsEstNextYear = m.getDecimal("epsEstNextYear"),
        /* e9 */ epsEstNextQtr = m.getDecimal("epsEstNextQtr"),
        /* f6 */ floatShares = m.getDecimal("floatShares"),
        /* g0 */ low = m.getDecimal("low"),
        /* g1 */ holdingsGainPct = m.getDecimal("holdingsGainPct"),
        /* g3 */ annualizedGain = m.getDecimal("annualizedGain"),
        /* g4 */ holdingsGain = m.getDecimal("holdingsGain"),
        /* g5 */ holdingsGainPctRealTime = m.getDecimal("holdingsGainPctRealTime"),
        /* g6 */ holdingsGainRealTime = m.getDecimal("holdingsGainRealTime"),
        /* h0 */ high = m.getDecimal("high"),
        /* i0 */ moreInfo = m.getString("moreInfo"),
        /* i5 */ orderBookRealTime = m.getDecimal("orderBookRealTime"),
        /* j0 */ low52Week = m.getDecimal("low52Week"),
        /* j1 */ marketCap = m.getDecimal("marketCap"),
        /* j2 */ sharesOutstanding = m.getDecimal("sharesOutstanding"),
        /* j3 */ marketCapRealTime = m.getDecimal("marketCapRealTime"),
        /* j4 */ EBITDA = m.getDecimal("EBITDA"),
        /* j5 */ change52WeekLow = m.getDecimal("change52WeekLow"),
        /* j6 */ changePct52WeekLow = m.getDecimal("changePct52WeekLow"),
        /* k0 */ high52Week = m.getDecimal("high52Week"),
        /* k2 */ changePctRealTime = m.getDecimal("changePctRealTime"),
        /* k3 */ lastTradeSize = m.getInteger("lastTradeSize"),
        /* k4 */ change52WeekHigh = m.getDecimal("change52WeekHigh"),
        /* k5 */ changePct52WeekHigh = m.getDecimal("changePct52WeekHigh"),
        /* l1 */ lastTrade = m.getDecimal("lastTrade"),
        /* l2 */ highLimit = m.getDecimal("highLimit"),
        /* l3 */ lowLimit = m.getDecimal("lowLimit"),
        /* m3 */ movingAverage50Day = m.getDecimal("movingAverage50Day"),
        /* m4 */ movingAverage200Day = m.getDecimal("movingAverage200Day"),
        /* m5 */ change200DayMovingAvg = m.getDecimal("change200DayMovingAvg"),
        /* m6 */ changePct200DayMovingAvg = m.getDecimal("changePct200DayMovingAvg"),
        /* m7 */ change50DayMovingAvg = m.getDecimal("change50DayMovingAvg"),
        /* m8 */ changePct50DayMovingAvg = m.getDecimal("changePct50DayMovingAvg"),
        /* n0 */ name = m.getString("name"),
        /* n4 */ notes = m.getString("notes"),
        /* o0 */ open = m.getDecimal("open"),
        /* p0 */ prevClose = m.getDecimal("prevClose"),
        /* p1 */ pricePaid = m.getDecimal("pricePaid"),
        /* p2 */ changePct = m.getDecimal("changePct"),
        /* p5 */ priceOverSales = m.getDecimal("priceOverSales"),
        /* p6 */ priceOverBook = m.getDecimal("priceOverBook"),
        /* q0 */ exDividendDate = m.getDate("exDividendDate"),
        /* q2 */ close = m.getDecimal("close"),
        /* r0 */ peRatio = m.getDecimal("peRatio"),
        /* r1 */ dividendPayDate = m.getDate("dividendPayDate"),
        /* r2 */ peRatioRealTime = m.getDecimal("peRatioRealTime"),
        /* r5 */ pegRatio = m.getDecimal("pegRatio"),
        /* r6 */ priceOverEPSCurYr = m.getDecimal("priceOverEPSCurYr"),
        /* r7 */ priceOverEPSNextYr = m.getDecimal("priceOverEPSNextYr"),
        /* s0 */ oldSymbol = m.uget("symbol"),
        /* ?? */ newSymbol = getChangedSymbol(m.uget("errorMessage")),
        /* s1 */ sharesOwned = m.getString("sharesOwned"),
        /* s6 */ revenue = m.getDecimal("revenue"),
        /* s7 */ shortRatio = m.getDecimal("shortRatio"),
        /* t1 */ tradeTime = m.getString("tradeTime"),
        /* t8 */ target1Yr = m.getDecimal("target1Y"),
        /* v0 */ volume = m.getDecimal("volume"),
        /* v1 */ holdingsValue = m.getDecimal("holdingsValue"),
        /* v7 */ holdingsValueRealTime = m.getDecimal("holdingsValueRealTime"),
        /* w1 */ daysChange = m.getDecimal("daysChange"),
        /* w4 */ daysChangeRealTime = m.getDecimal("daysChangeRealTime"),
        /* x0 */ exchange = m.uget("exchange") map (_.toUpperCase),
        /* y0 */ divYield = m.getDecimal("divYield"),
        /* ++ */ responseTimeMsec = responseTimeMsec)
    }
  }

  private def mapCodeToNamedValues(code: String, data: String): Option[Seq[(String, String)]] = {
    val aValue = stringValue(data).toOption
    code match {
      case key if !CodeToFieldNames.contains(key) =>
        console.error(s"Code '$code' was not recognized (value = '$aValue')")
        None
      case "c0" => extractChangeAndPercent(data)
      case "c8" => extractChangeAterHours(data)
      case "k1" => extractLastTradeWithTime(data)
      case "l0" => extractLastTradeWithTime(data)
      case "m0" => extract52WeekRange(data)
      case "m2" => extract52WeekRangeRealTime(data)
      case "w0" => extract52WeekRange(data)
      case key =>
        for {
          name <- CodeToFieldNames.get(key)
          value <- aValue
        } yield Seq(name -> value)
    }
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

  private def appendLastTradeDateTime(kvps: Map[String, String]): Map[String, String] = {
    (kvps.get("tradeDate"), kvps.get("tradeTime")) match {
      case (Some(tradeDate), Some(tradeTime)) =>
        Try {
          // "9/01/2016 6:17a"
          val dateString = s"$tradeDate $tradeTime"

          // format as an ISO date string
          Moment(dateString, "M/DD/YYYY h:mma").tz("America/New_York").toISOString()
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

}

/**
 * Yahoo Finance! CSV Quotes Parser Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object YahooFinanceCSVQuotesParser {
  // parsing constants
  val NOT_APPLICABLE = Set("NaN", "-", "N/A", "\"N/A\"", null)
  val BOLD_START = "<b>"
  val BOLD_END = "</b>"

  /**
   * Parses the encoded string value into a decimal value
   * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
   * @return the [[Double]] value or <tt>null</tt> if a parsing error occurred
   */
  private def decimalValue(field: String, encodedString: String): js.UndefOr[Double] = {
    val rawString = encodedString.replaceAll("[$]", "").replaceAll("[+]", "").replaceAll("[,]", "")
    try {
      stringValue(field, rawString) flatMap {
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
        console.error(s"$field: Error parsing decimal value '$rawString' ($encodedString)", e)
        js.undefined
    }
  }

  /**
   * Parses the string value into a date value
   * @param encodedString the given string value
   * @return the [[js.Date date]] or <tt>null</tt> if a parsing error occurred
   */
  private def dateValue(field: String, encodedString: String): js.UndefOr[js.Date] = {
    stringValue(field, encodedString) map (s => new js.Date(s))
  }

  /**
   * Parses the string value into an integer value
   * @param encodedString the given string value
   * @return the [[Int]] value or <tt>null</tt> if a parsing error occurred
   */
  private def intValue(field: String, encodedString: String): js.UndefOr[Int] = {
    decimalValue(field, encodedString) map (_.toInt)
  }

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

  /**
   * Extracts the display-friendly string value from the encoded string value
   * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
   * @return the string value (e.g. <tt>614.33</tt>)
   */
  private def stringValue(field: String, encodedString: String): js.UndefOr[String] = {
    // if null or contains null indicator, return null
    if (encodedString.isBlank || NOT_APPLICABLE.contains(encodedString)) js.undefined
    else {
      // trim the value
      val value = encodedString.trim()

      // if wrapped in quotes
      if (value.startsWith("\"") && value.endsWith("\"")) stringValue(field, value.drop(1).dropRight(1))

      // if wrapped in bold tags
      else if (value.toLowerCase.startsWith(BOLD_START) && value.toLowerCase.endsWith(BOLD_END))
        stringValue(field, value.substring(BOLD_START.length(), value.length() - BOLD_END.length()))

      else value
    }
  }

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
   * Map Extensions
   * @param m the given [[Map map]]
   */
  implicit class MapExtensions(val m: Map[String, String]) extends AnyVal {

    @inline
    def getDate(key: String): js.UndefOr[Date] = uget(key) flatMap (dateValue(key, _))

    @inline
    def getDecimal(key: String): js.UndefOr[Double] = uget(key) flatMap (decimalValue(key, _))

    @inline
    def getInteger(key: String): js.UndefOr[Int] = uget(key) flatMap (intValue(key, _))

    @inline
    def uget(key: String): js.UndefOr[String] = m.get(key).orUndefined

    @inline
    def getString(key: String): js.UndefOr[String] = uget(key) flatMap (stringValue(key, _))

  }

}
