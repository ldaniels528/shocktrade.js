package com.shocktrade.server.services.yahoo

import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesParser._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService._
import com.shocktrade.util.ParsingHelper._
import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.moment.timezone._
import org.scalajs.nodejs.{NodeRequire, console}

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success, Try}

/**
  * Yahoo Finance! CSV Quotes Parser
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceCSVQuotesParser()(implicit require: NodeRequire) {
  private val moment = Moment()
  MomentTimezone()

  /**
    * Parses the given encoded stock quote string into a stock quote object
    * @param paramdata the given encoded parameter string
    * @param csvdata   the given stock quote data
    * @return the [[YFCSVQuote quote]]
    */
  def parseQuote(symbol: String, paramdata: String, csvdata: String, startTime: Double): YFCSVQuote = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the fields and parameter data
    val fields: Seq[String] = parseCSVData(csvdata.trim)
    val params: Seq[String] = parseParams(paramdata.toLowerCase)
    val kvps = js.Dictionary(params zip fields: _*)

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
      /* a0 */ m.getDecimal("ask"),
      /* a2 */ m.getDecimal("avgVol"),
      /* a5 */ m.getInteger("askSize"),
      /* b0 */ m.getDecimal("bid"),
      /* b2 */ m.getDecimal("askRealTime"),
      /* b3 */ m.getDecimal("bidRealTime"),
      /* b4 */ m.getDecimal("bookValuePerShare"),
      /* b6 */ m.getInteger("bidSize"),
      /* c1 */ m.getDecimal("change"),
      /* c3 */ m.getDecimal("commission"),
      /* c4 */ m.getString("currencyCode"),
      /* c6 */ m.getDecimal("changeRealTime"),
      /* c8 */ m.getDecimal("changeAfterHours"),
      /* d0 */ m.getDecimal("divShare"),
      /* d1 */ m.uget("tradeDate"),
      /* ++ */ m.uget("tradeDateTime") map (s => new js.Date(s)),
      /* e0 */ m.getDecimal("eps"),
      /* e1 */ m.getString("errorMessage"),
      /* e7 */ m.getDecimal("epsEstCurrentYear"),
      /* e8 */ m.getDecimal("epsEstNextYear"),
      /* e9 */ m.getDecimal("epsEstNextQtr"),
      /* f6 */ m.getDecimal("floatShares"),
      /* g0 */ m.getDecimal("low"),
      /* g1 */ m.getDecimal("holdingsGainPct"),
      /* g3 */ m.getDecimal("annualizedGain"),
      /* g4 */ m.getDecimal("holdingsGain"),
      /* g5 */ m.getDecimal("holdingsGainPctRealTime"),
      /* g6 */ m.getDecimal("holdingsGainRealTime"),
      /* h0 */ m.getDecimal("high"),
      /* i0 */ m.getString("moreInfo"),
      /* i5 */ m.getDecimal("orderBookRealTime"),
      /* j0 */ m.getDecimal("low52Week"),
      /* j1 */ m.getDecimal("marketCap"),
      /* j2 */ m.getDecimal("sharesOutstanding"),
      /* j3 */ m.getDecimal("marketCapRealTime"),
      /* j4 */ m.getDecimal("EBITDA"),
      /* j5 */ m.getDecimal("change52WeekLow"),
      /* j6 */ m.getDecimal("changePct52WeekLow"),
      /* k0 */ m.getDecimal("high52Week"),
      /* k2 */ m.getDecimal("changePctRealTime"),
      /* k3 */ m.getInteger("lastTradeSize"),
      /* k4 */ m.getDecimal("change52WeekHigh"),
      /* k5 */ m.getDecimal("changePct52WeekHigh"),
      /* l1 */ m.getDecimal("lastTrade"),
      /* l2 */ m.getDecimal("highLimit"),
      /* l3 */ m.getDecimal("lowLimit"),
      /* m3 */ m.getDecimal("movingAverage50Day"),
      /* m4 */ m.getDecimal("movingAverage200Day"),
      /* m5 */ m.getDecimal("change200DayMovingAvg"),
      /* m6 */ m.getDecimal("changePct200DayMovingAvg"),
      /* m7 */ m.getDecimal("change50DayMovingAvg"),
      /* m8 */ m.getDecimal("changePct50DayMovingAvg"),
      /* n0 */ m.getString("name"),
      /* n4 */ m.getString("notes"),
      /* o0 */ m.getDecimal("open"),
      /* p0 */ m.getDecimal("prevClose"),
      /* p1 */ m.getDecimal("pricePaid"),
      /* p2 */ m.getDecimal("changePct"),
      /* p5 */ m.getDecimal("priceOverSales"),
      /* p6 */ m.getDecimal("priceOverBook"),
      /* q0 */ m.getDate("exDividendDate"),
      /* q2 */ m.getDecimal("close"),
      /* r0 */ m.getDecimal("peRatio"),
      /* r1 */ m.getDate("dividendPayDate"),
      /* r2 */ m.getDecimal("peRatioRealTime"),
      /* r5 */ m.getDecimal("pegRatio"),
      /* r6 */ m.getDecimal("priceOverEPSCurYr"),
      /* r7 */ m.getDecimal("priceOverEPSNextYr"),
      /* s0 */ m.getString("symbol"),
      /* ?? */ getChangedSymbol(m.uget("errorMessage")),
      /* s1 */ m.getString("sharesOwned"),
      /* s6 */ m.getDecimal("revenue"),
      /* s7 */ m.getDecimal("shortRatio"),
      /* t1 */ m.getString("tradeTime"),
      /* t8 */ m.getDecimal("target1Y"),
      /* v0 */ m.getDecimal("volume"),
      /* v1 */ m.getDecimal("holdingsValue"),
      /* v7 */ m.getDecimal("holdingsValueRealTime"),
      /* w1 */ m.getDecimal("daysChange"),
      /* w4 */ m.getDecimal("daysChangeRealTime"),
      /* x0 */ m.uget("exchange") map (_.toUpperCase),
      /* y0 */ m.getDecimal("divYield"),
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
    def uget(key: String) = m.get(key).orUndefined

    @inline
    def getDate(key: String) = uget(key) flatMap (dateValue(key, _))

    @inline
    def getDecimal(key: String) = uget(key) flatMap (decimalValue(key, _))

    @inline
    def getInteger(key: String) = uget(key) flatMap (intValue(key, _))

    @inline
    def getString(key: String) = uget(key) flatMap (stringValue(key, _))

  }

}
