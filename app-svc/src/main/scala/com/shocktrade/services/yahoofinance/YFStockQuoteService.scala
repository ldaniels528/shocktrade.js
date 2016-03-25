package com.shocktrade.services.yahoofinance

import java.text.SimpleDateFormat
import java.util.Date

import com.shocktrade.services.HttpUtil
import com.shocktrade.services.util.StringUtil._
import com.shocktrade.services.util._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Yahoo! Finance: CSV Stock Quote Service
 * @author lawrence.daniels@gmail.com
 */
object YFStockQuoteService extends HttpUtil {
  private lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  // parsing constants
  val NOT_APPLICABLE = Set("NaN", "-", "N/A", "\"N/A\"", null)
  val BOLD_START = "<b>"
  val BOLD_END = "</b>"

  /**
   * Returns the parameter codes required to retrieve values for the given fields.
   */
  def getParams(fields: String*): String = {
    (fields flatMap FIELD_CODE_TO_MAPPING.get).map(c => if(c.endsWith("0")) c.dropRight(1) else c).mkString
  }

  def getAllParams: String = FIELD_CODE_TO_MAPPING.keys.mkString

  /**
   * Performs the service call and returns a single object read from the service.
   * @param symbol the given stock symbol (e.g., "AAPL")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the [[YFStockQuote]]
   */
  def getQuoteSync(symbol: String, params: String): Option[YFStockQuote] = {
    // capture the start time
    val startTime = System.currentTimeMillis()

    // create the service URL
    val urlString = s"http://finance.yahoo.com/d/quotes.csv?s=$symbol&f=$params"

    // retrieve and parse the data
    val lines = Source.fromBytes(getResource(urlString)).getLines().toSeq

    // retrieve and parse the data
    val quotes = lines map (parseQuote(symbol, params, _, startTime))
    quotes.headOption
  }

  /**
   * Retrieves a quote from Google Finance by symbol
   * @param symbols the given sequence of tickers (e.g. "VFINX")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the sequence of [[YFStockQuote]]
   */
  def getQuotesSync(symbols: Seq[String], params: String): Seq[YFStockQuote] = {
    // capture the start time
    val startTime = System.currentTimeMillis()

    // create the symbol list
    val symbolList = symbols mkString "+"

    // create the service URL
    val urlString = s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params"

    // retrieve and parse the data
    val lines = Source.fromBytes(getResource(urlString)).getLines()

    // create the tuple of symbol to data
    val tuples = symbols zip lines.toSeq
    tuples map { case (symbol, line) => parseQuote(symbol, params, line, startTime) }
  }

  /**
   * Performs the service call and returns a single object read from the service.
   * @param symbol the given stock symbol (e.g., "AAPL")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the [[YFStockQuote]]
   */
  def getQuote(symbol: String, params: String)(implicit ec: ExecutionContext): Future[YFStockQuote] = {
    // capture the start time
    val startTime = System.currentTimeMillis()

    for {
    // retrieve the data
      doc <- Future {
        getResource(s"http://finance.yahoo.com/d/quotes.csv?s=$symbol&f=$params")
      }

      // retrieve a single line of data
      line = Source.fromBytes(doc).getLines().next()
      _ = logger.debug(line)

    // retrieve and parse the data
    } yield parseQuote(symbol, params, line, startTime)
  }

  /**
   * Retrieves a quote from Google Finance by symbol
   * @param symbol the given equity index ticker (e.g. {{{ ^IXIC }}})
   * @param params the given stock fields (e.g., "soxq2")
   * @return the sequence of [[YFStockQuote]]
   */
  def getIndexQuotes(symbol: String, params: String)(implicit ec: ExecutionContext): Future[Seq[YFStockQuote]] = {
    import scala.io.Source

    // capture the start time
    val startTime = System.currentTimeMillis()

    for {
    // retrieve the data
      doc <- Future {
        getResource(s"http://download.finance.yahoo.com/d/quotes.csv?s=@$symbol&f=$params")
      }

      // retrieve the lines of data
      lines = Source.fromBytes(doc).getLines()

    // transform the lines into quotes
    } yield lines map {
      parseQuote(symbol, params, _, startTime)
    } toSeq
  }

  /**
   * Retrieves a quote from Google Finance by symbol
   * @param symbols the given sequence of tickers (e.g. "VFINX")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the sequence of [[YFStockQuote]]
   */
  def getQuotes(symbols: Seq[String], params: String)(implicit ec: ExecutionContext): Future[Seq[YFStockQuote]] = {
    import scala.io.Source

    // create the symbol list
    val symbolList = symbols mkString "+"

    // capture the start time
    val startTime = System.currentTimeMillis()

    for {
    // retrieve the CSV data (as bytes) by symbols
      doc <- Future {
        getResource(s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params")
      }

      // transform the bytes into quotes
      lines = Source.fromBytes(doc).getLines()

      // create the tuple of symbol to data
      tuples = symbols zip lines.toSeq
    } yield tuples map { case (symbol, line) => parseQuote(symbol, params, line, startTime) }
  }

  /**
   * Retrieves quotes from Yahoo! Finance by symbol
   * @param symbols the given sequence of tickers (e.g. "VFINX")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the iteration of data
   */
  def getCSVDataSync(symbols: Seq[String], params: String)(implicit ec: ExecutionContext): Iterator[String] = {
    import scala.io.Source

    // create the symbol list
    val symbolList = symbols mkString "+"

    // retrieve the CSV data (as bytes) by symbols
    val doc = getResource(s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params")

    // transform the bytes into lines of data
    Source.fromBytes(doc).getLines()
  }

  /**
   * Retrieves quotes from Yahoo! Finance by symbol
   * @param symbols the given sequence of tickers (e.g. "VFINX")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the iteration of data
   */
  def getCSVData(symbols: Seq[String], params: String)(implicit ec: ExecutionContext): Future[Iterator[String]] = {
    import scala.io.Source

    // create the symbol list
    val symbolList = symbols mkString "+"

    for {
    // retrieve the CSV data (as bytes) by symbols
      doc <- Future {
        getResource(s"http://finance.yahoo.com/d/quotes.csv?s=$symbolList&f=$params")
      }

    // transform the bytes into lines of data
    } yield Source.fromBytes(doc).getLines()
  }

  /**
   * Parses the given encoded stock quote string into a stock quote object
   * @param paramdata the given encoded parameter string
   * @param csvdata the given stock quote data
   * @return the [[YFStockQuote]]
   */
  def parseQuote(symbol: String, paramdata: String, csvdata: String, startTime: Long): YFStockQuote = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the fields and parameter data
    val fields: Seq[String] = parseCSVData(csvdata.trim)
    val params: Seq[String] = parseParams(paramdata.toLowerCase)
    val kvps = Map(params.zip(fields): _*)

    // transform the mapping into a bean
    toQuote(symbol, kvps, responseTimeMsec)
  }

  private def parseCSVData(command: String): Seq[String] = {
    val sb = new StringBuilder()
    var inDQ = false

    def newToken = {
      val tok = sb.toString(); sb.clear(); if (tok != "") Seq(tok) else Seq.empty
    }
    def toggleDQuotes = {
      inDQ = !inDQ; Seq.empty
    }
    def append(c: Char) = {
      sb += c; Seq.empty
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

  private def toQuote(symbol: String, kvps: Map[String, String], responseTimeMsec: Long): YFStockQuote = {
    // convert the key-value pairs to method-value pairs
    val t = Map((kvps flatMap { case (k, v) => mapCodeToNamedValues(k, v) } toSeq) flatten: _*)
    val m = appendLastTradeDateTime(t)
    new YFStockQuote(
      symbol,
      /* a0 */ m.get("ask") flatMap decimalValue,
      /* a2 */ m.get("avgVol") flatMap longValue,
      /* a5 */ m.get("askSize") flatMap intValue,
      /* b0 */ m.get("bid") flatMap decimalValue,
      /* b2 */ m.get("askRealTime") flatMap decimalValue,
      /* b3 */ m.get("bidRealTime") flatMap decimalValue,
      /* b4 */ m.get("bookValuePerShare") flatMap decimalValue,
      /* b6 */ m.get("bidSize") flatMap intValue,
      /* c1 */ m.get("change") flatMap decimalValue,
      /* c3 */ m.get("commission") flatMap decimalValue,
      /* c4 */ m.get("currencyCode") flatMap stringValue,
      /* c6 */ m.get("changeRealTime") flatMap decimalValue,
      /* c8 */ m.get("changeAfterHours") flatMap decimalValue,
      /* d0 */ m.get("divShare") flatMap decimalValue,
      /* d1 */ m.get("tradeDate") flatMap dateValue,
      /* ++ */ m.get("tradeDateTime") map (s => new SimpleDateFormat("MM/dd/yyyy hh:mma z").parse(s)),
      /* e0 */ m.get("eps") flatMap decimalValue,
      /* e1 */ m.get("errorMessage") flatMap stringValue,
      /* e7 */ m.get("epsEstCurrentYear") flatMap decimalValue,
      /* e8 */ m.get("epsEstNextYear") flatMap decimalValue,
      /* e9 */ m.get("epsEstNextQtr") flatMap decimalValue,
      /* f6 */ m.get("floatShares") flatMap decimalValue,
      /* g0 */ m.get("low") flatMap decimalValue,
      /* g1 */ m.get("holdingsGainPct") flatMap decimalValue,
      /* g3 */ m.get("annualizedGain") flatMap decimalValue,
      /* g4 */ m.get("holdingsGain") flatMap decimalValue,
      /* g5 */ m.get("holdingsGainPctRealTime") flatMap decimalValue,
      /* g6 */ m.get("holdingsGainRealTime") flatMap decimalValue,
      /* h0 */ m.get("high") flatMap decimalValue,
      /* i0 */ m.get("moreInfo") flatMap stringValue,
      /* i5 */ m.get("orderBookRealTime") flatMap decimalValue,
      /* j0 */ m.get("low52Week") flatMap decimalValue,
      /* j1 */ m.get("marketCap") flatMap decimalValue,
      /* j2 */ m.get("sharesOutstanding") flatMap longValue,
      /* j3 */ m.get("marketCapRealTime") flatMap decimalValue,
      /* j4 */ m.get("EBITDA") flatMap decimalValue,
      /* j5 */ m.get("change52WeekLow") flatMap decimalValue,
      /* j6 */ m.get("changePct52WeekLow") flatMap decimalValue,
      /* k0 */ m.get("high52Week") flatMap decimalValue,
      /* k2 */ m.get("changePctRealTime") flatMap decimalValue,
      /* k3 */ m.get("lastTradeSize") flatMap intValue,
      /* k4 */ m.get("change52WeekHigh") flatMap decimalValue,
      /* k5 */ m.get("changePct52WeekHigh") flatMap decimalValue,
      /* l1 */ m.get("lastTrade") flatMap decimalValue,
      /* l2 */ m.get("highLimit") flatMap decimalValue,
      /* l3 */ m.get("lowLimit") flatMap decimalValue,
      /* m3 */ m.get("movingAverage50Day") flatMap decimalValue,
      /* m4 */ m.get("movingAverage200Day") flatMap decimalValue,
      /* m5 */ m.get("change200DayMovingAvg") flatMap decimalValue,
      /* m6 */ m.get("changePct200DayMovingAvg") flatMap decimalValue,
      /* m7 */ m.get("change50DayMovingAvg") flatMap decimalValue,
      /* m8 */ m.get("changePct50DayMovingAvg") flatMap decimalValue,
      /* n0 */ m.get("name") flatMap stringValue,
      /* n4 */ m.get("notes") flatMap stringValue,
      /* o0 */ m.get("open") flatMap decimalValue,
      /* p0 */ m.get("prevClose") flatMap decimalValue,
      /* p1 */ m.get("pricePaid") flatMap decimalValue,
      /* p2 */ m.get("changePct") flatMap decimalValue,
      /* p5 */ m.get("priceOverSales") flatMap decimalValue,
      /* p6 */ m.get("priceOverBook") flatMap decimalValue,
      /* q0 */ m.get("exDividendDate") flatMap dateValue,
      /* q2 */ m.get("close") flatMap decimalValue,
      /* r0 */ m.get("peRatio") flatMap decimalValue,
      /* r1 */ m.get("dividendPayDate") flatMap dateValue,
      /* r2 */ m.get("peRatioRealTime") flatMap decimalValue,
      /* r5 */ m.get("pegRatio") flatMap decimalValue,
      /* r6 */ m.get("priceOverEPSCurYr") flatMap decimalValue,
      /* r7 */ m.get("priceOverEPSNextYr") flatMap decimalValue,
      /* s0 */ m.get("symbol") flatMap stringValue,
      /* ?? */ getChangedSymbol(m.get("errorMessage")),
      /* s1 */ m.get("sharesOwned") flatMap stringValue,
      /* s6 */ m.get("revenue") flatMap decimalValue,
      /* s7 */ m.get("shortRatio") flatMap decimalValue,
      /* t1 */ m.get("tradeTime") flatMap stringValue,
      /* t8 */ m.get("target1Y") flatMap decimalValue,
      /* v0 */ m.get("volume") flatMap longValue,
      /* v1 */ m.get("holdingsValue") flatMap decimalValue,
      /* v7 */ m.get("holdingsValueRealTime") flatMap decimalValue,
      /* w1 */ m.get("daysChange") flatMap decimalValue,
      /* w4 */ m.get("daysChangeRealTime") flatMap decimalValue,
      /* x0 */ m.get("exchange") map (_.toUpperCase),
      /* y0 */ m.get("divYield") flatMap decimalValue,
      /* ++ */ responseTimeMsec)
  }

  private def mapCodeToNamedValues(code: String, data: String): Option[Seq[(String, String)]] = {
    val value = stringValue(data)
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
        logger.error(s"Code '$code' was not recognized (value = '$value')")
        None
    }
  }

  /**
   * Optionally extracts the new ticker from given HTML string
   */
  private def getChangedSymbol(htmlString: Option[String]): Option[String] = {
    import ParsingUtilities._

    // error message is: "Ticker symbol has changed to <a href="/q?s=TNCC.PK">TNCC.PK</a>"
    for {
      html <- htmlString

      (p0, p1) <- html.tagContent("a")

      msg = html.substring(p0, p1).trim

      cleanmsg <- if (msg.length > 0) Some(msg) else None
    } yield cleanmsg
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
          // parse the date string into a Date instance
          val date = RegexDateParser.parseDate(tradeDate)

          // parse the date/time string into a date-based time stamp
          val time = if (RegexDateParser.isTime(tradeTime)) tradeTime.toUpperCase else "4:00PM"
          val tz = if (DateUtil.isDaylightSavings) "EDT" else "EST"
          s"${new SimpleDateFormat("MM/dd/yyyy").format(date)} $time $tz"
        } match {
          case Success(ts) => kvps + ("tradeDateTime" -> ts)
          case Failure(e) =>
            logger.error(s"Error parsing date/time string (date = '$tradeDate', time = '$tradeTime')")
            kvps
        }
      case _ => kvps
    }
  }

  /**
   * Extracts a value tuple from the given encoded string
   * @param encodedString the given encoded string
   * @return a tuple of options of a string
   */
  private def tuplize(encodedString: String): (Option[String], Option[String]) = {
    stringValue(encodedString) match {
      case None => (None, None)
      case Some(text) =>
        text.split(" - ") map (_.trim) match {
          case Array(a, b, _*) => (Some(a), Some(b))
          case searchText =>
            logger.warn(s"Separator '$searchText' was not found in '$encodedString'")
            (Some(text), None)
        }
    }
  }

  /**
   * Parses the string value into a date value
   * @param encodedString the given string value
   * @return the [[Date]] value or <tt>null</tt> if a parsing error occurred
   */
  private def dateValue(encodedString: String): Option[Date] = {
    stringValue(encodedString) match {
      case None => None
      case Some(value) =>
        Try(RegexDateParser.parseDate(value)) match {
          case Success(date) => Some(date)
          case Failure(e) =>
            logger.error(e.getMessage)
            None
        }
    }
  }

  /**
   * Parses the encoded string value into a decimal value
   * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
   * @return the [[Double]] value or <tt>null</tt> if a parsing error occurred
   */
  private def decimalValue(encodedString: String): Option[Double] = {
    val rawString = encodedString.replaceAll("[$]", "").replaceAll("[+]", "").replaceAll("[,]", "")
    try {
      stringValue(rawString) match {
        case None => None

        // is it blank?
        case Some(s) if s.isBlank || NOT_APPLICABLE.contains(s) => None

        // *%, *K, *M, *B, *T
        case Some(s) if s.endsWith("%") => Some(s.dropRight(1).toDouble)
        case Some(s) if s.endsWith("K") => Some(s.dropRight(1).toDouble * 1.0e3)
        case Some(s) if s.endsWith("M") => Some(s.dropRight(1).toDouble * 1.0e6)
        case Some(s) if s.endsWith("B") => Some(s.dropRight(1).toDouble * 1.0e9)
        case Some(s) if s.endsWith("T") => Some(s.dropRight(1).toDouble * 1.0e12)

        // otherwise, just convert it
        case Some(s) => Some(s.toDouble)
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error parsing decimal value '$rawString' ($encodedString)", e)
        None
    }
  }

  /**
   * Parses the string value into a long integer value
   * @param encodedString the given string value
   * @return the [[Long]] value or <tt>null</tt> if a parsing error occurred
   */
  private def longValue(encodedString: String): Option[Long] = decimalValue(encodedString) map (_.toLong)

  /**
   * Parses the string value into an integer value
   * @param encodedString the given string value
   * @return the [[Int]] value or <tt>null</tt> if a parsing error occurred
   */
  private def intValue(encodedString: String): Option[Int] = decimalValue(encodedString) map (_.toInt)

  /**
   * Parses the string value into a short integer value
   * @param encodedString the given string value
   * @return the [[Short]] value or <tt>null</tt> if a parsing error occurred
   */
  private def shortValue(encodedString: String): Option[Short] = decimalValue(encodedString) map (_.toShort)

  /**
   * Extracts the display-friendly string value from the encoded string value
   * @param encodedString the given encoded string value (e.g. <tt>"&ltb&gt614.33&lt/b&gt"</tt>)
   * @return the string value (e.g. <tt>614.33</tt>)
   */
  private def stringValue(encodedString: String): Option[String] = {
    // if null or contains null indicator, return null  
    if (StringUtil.isBlank(encodedString) || NOT_APPLICABLE.contains(encodedString)) None
    else {
      // trim the value
      val value = encodedString.trim()

      // if wrapped in quotes
      if (value.startsWith("\"") && value.endsWith("\"")) stringValue(value.drop(1).dropRight(1))

      // if wrapped in bold tags
      else if (value.toLowerCase.startsWith(BOLD_START) && value.toLowerCase.endsWith(BOLD_END))
        stringValue(value.substring(BOLD_START.length(), value.length() - BOLD_END.length()))

      else Some(value)
    }
  }

  // Yahoo! code to field mapping
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

  /**
   * Represents a Yahoo! Finance Stock Quote (Web Service Object)
   * @see http://www.gummy-stuff.org/Yahoo-data.htm
   * @see http://www.codeproject.com/Articles/37550/Stock-quote-and-chart-from-Yahoo-in-C
   * @see http://people.stern.nyu.edu/adamodar/New_Home_Page/data.html
   * @see http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
   * @see http://code.google.com/p/yahoo-finance-managed/wiki/CSVAPI
   * @author lawrence.daniels@gmail.com
   */
  class YFStockQuote(val symbol: String,
                     val ask: Option[Double], // a0 - Ask
                     val avgVol: Option[Long], // a2 - Average Daily Volume
                     val askSize: Option[Int], // a5 - Ask Size
                     val bid: Option[Double], // b0 - Bid
                     // b1 - ???? Ask/Bid?
                     val askRealTime: Option[Double], // b2 - Ask (Real-time)
                     val bidRealTime: Option[Double], // b3 - Bid (Real-time)
                     val bookValuePerShare: Option[Double], // b4 - Book Value Per Share
                     val bidSize: Option[Int], // b6 - Bid Size
                     // c0 - Change & Percent Change
                     val change: Option[Double], // c1 - Change
                     val commission: Option[Double], // c3 - Commission
                     val currencyCode: Option[String], // c4 - Currency Code
                     val changeRealTime: Option[Double], // c6 - Change (Real-time)
                     val changeAfterHours: Option[Double], // c8 - After Hours Change (Real-time)
                     val divShare: Option[Double], // d0 - Dividend/Share (Trailing Annual)
                     val tradeDate: Option[Date], // d1 - Last Trade Date
                     val tradeDateTime: Option[Date],
                     // d2 - Trade Date? (-)
                     // d3 - Last Trade Time? ("Feb  3", "10:56am")
                     val eps: Option[Double], // e0 - Earnings/Share (Diluted)
                     val errorMessage: Option[String], // e1 - Error Indication (returned for symbol changed / invalid)
                     val epsEstCurrentYear: Option[Double], // e7 - EPS Estimate Current Year
                     val epsEstNextYear: Option[Double], // e8 - EPS Estimate Next Year
                     val epsEstNextQtr: Option[Double], // e9 - EPS Estimate Next Quarter
                     val floatShares: Option[Double], // f6 - Float Shares
                     val low: Option[Double], // g0 - Day's Low
                     val holdingsGainPct: Option[Double], // g1 - Holdings Gain Percent
                     val annualizedGain: Option[Double], // g3 - Annualized Gain
                     val holdingsGain: Option[Double], // g4 - Holdings Gain
                     val holdingsGainPctRealTime: Option[Double], // g5 - Holdings Gain Percent (Real-time)
                     val holdingsGainRealTime: Option[Double], // g6 - Holdings Gain (Real-time)
                     val high: Option[Double], // h0 - Day's High
                     val moreInfo: Option[String], // i0 - More Info
                     val orderBookRealTime: Option[Double], // i5 - Order Book (Real-time)
                     val low52Week: Option[Double], // j0 - 52-week Low
                     val marketCap: Option[Double], // j1 - Market Capitalization
                     val sharesOutstanding: Option[Long], // j2 - Shares Outstanding
                     val marketCapRealTime: Option[Double], // j3 - Market Capitalization (Real-time)
                     val EBITDA: Option[Double], // j4 - EBITDA
                     val change52WeekLow: Option[Double], // j5 - Change From 52-week Low
                     val changePct52WeekLow: Option[Double], // j6 - Percent Change From 52-week Low
                     val high52Week: Option[Double], // k0 - 52-week High
                     // k1 - Last Trade With Time (Real-time)
                     val changePctRealTime: Option[Double], // k2 - Change Percent (Real-time)
                     val lastTradeSize: Option[Int], // k3 - Last Trade Size
                     val change52WeekHigh: Option[Double], // k4 - Change From 52-week High
                     val changePct52WeekHigh: Option[Double], // k5 - Percent Change From 52-week High
                     // l0 - Last Trade (With Time)
                     val lastTrade: Option[Double], // l1 - Last Trade (Price Only)
                     val highLimit: Option[Double], // l2 - High Limit
                     val lowLimit: Option[Double], // l3 - Low Limit
                     // m0 - Day's Range
                     // m2 - Day's Range (Real-time)
                     val movingAverage50Day: Option[Double], // m3 - 50-day Moving Average
                     val movingAverage200Day: Option[Double], // m4 - 200-day Moving Average
                     val change200DayMovingAvg: Option[Double], //m5 - Change From 200-day Moving Average
                     val changePct200DayMovingAvg: Option[Double], // m6 - Percent Change From 200-day Moving Average
                     val change50DayMovingAvg: Option[Double], // m7 - Change From 50-day Moving Average
                     val changePct50DayMovingAvg: Option[Double], // m8 - Percent Change From 50-day Moving Average
                     val name: Option[String], // n0 - Name
                     val notes: Option[String], // n4 - Notes
                     val open: Option[Double], // o0 - Open
                     val prevClose: Option[Double], // p0 - Previous Close
                     val pricePaid: Option[Double], // p1 - Price Paid
                     val changePct: Option[Double], // p2 - Change in Percent
                     val priceOverSales: Option[Double], // p5 - Price/Sales
                     val priceOverBook: Option[Double], // p6 - Price/Book
                     val exDividendDate: Option[Date], // q0 - Ex-Dividend Date
                     // q1 - ????
                     val close: Option[Double], // q2 - Close
                     val peRatio: Option[Double], // r0 - P/E Ratio
                     val dividendPayDate: Option[Date], // r1 - Dividend Pay Date
                     val peRatioRealTime: Option[Double], // r2 - P/E Ratio (Real-time)
                     val pegRatio: Option[Double], // r5 - PEG Ratio (Price Earnings Growth)
                     val priceOverEPSCurYr: Option[Double], // r6 - Price/EPS Estimate Current Year
                     val priceOverEPSNextYr: Option[Double], // r7 - Price/EPS Estimate Next Year
                     val oldSymbol: Option[String], // s0 - Symbol
                     val newSymbol: Option[String], // (Derived from e1: error message)
                     val sharesOwned: Option[String], // s1 - Shares Owned
                     val revenue: Option[Double], // s6 - Revenue
                     val shortRatio: Option[Double], // s7 - Short Ratio
                     val tradeTime: Option[String], // t1 - Last Trade Time
                     // t6 - Trade Links
                     // t7 - Ticker Trend
                     val target1Yr: Option[Double], // t8 - 1-Year Target Price
                     val volume: Option[Long], // v0 - Volume
                     val holdingsValue: Option[Double], // v1 - Holdings Value
                     val holdingsValueRealTime: Option[Double], // v7 - Holdings Value (Real-time)
                     // w0 - 52-week Range
                     val daysChange: Option[Double], // w1 - Day's Value Change
                     val daysChangeRealTime: Option[Double], // w4 - Day's Value Change (Real-time)
                     val exchange: Option[String], // x0 - Stock Exchange
                     val divYield: Option[Double], // y0 - Dividend Yield (Trailing Annual)
                     val responseTimeMsec: Long) {

    /**
     * Indicates whether the response contains an error
     * @return true, if the error message is set
     * @see #getErrorMessage()
     */
    def hasError: Boolean = errorMessage exists (_.trim.nonEmpty)

    override def toString = s"$symbol last=$lastTrade open=$open high=$high low=$low close=$close [$responseTimeMsec]"

  }

}