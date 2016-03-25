package com.shocktrade.services.yahoofinance

import java.util.Date

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.xml.{Node, NodeSeq, XML}

/**
 * Yahoo! Finance: Real-time Stock Quote Service
 * @author lawrence.daniels@gmail.com
 */
object YFRealtimeStockQuoteService extends YFWebService {

  /**
   * Retrieves for a real-time quote for the given ticker symbol
   * @param symbol the given ticker symbol (e.g. "GOOG")
   * @return a { @link YFRealtimeQuote quote}
   */
  def getQuoteSync(symbol: String): YFRealtimeQuote = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML.withSAXParser(new SAXFactoryImpl().newSAXParser()).load(s"http://finance.yahoo.com/q?s=$symbol")
    parseDocument(symbol, doc, startTime)
  }

  /**
   * Retrieves for a real-time quote for the given ticker symbol
   * @param symbol the given ticker symbol (e.g. "GOOG")
   * @return a { @link Future future} of a { @link YFRealtimeQuote quote}
   */
  def getQuote(symbol: String)(implicit ec: ExecutionContext): Future[YFRealtimeQuote] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q?s=$symbol")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Retrieves for a sequence of real-time quotes for the given ticker symbols
   * @param symbols the given sequence of ticker symbols
   * @return a { @link Future future} of a sequence of { @link YFRealtimeQuote quotes}
   */
  def getQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[Seq[YFRealtimeQuote]] = {
    Future.traverse(symbols) { symbol => getQuote(symbol) }
  }

  /**
   * Transforms the document into a real-time quote
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): YFRealtimeQuote = {
    // capture the response time
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the header and data sections
    val (title, trade) = parseHeader(doc)
    val pairs = (doc \\ "table") flatMap parseTableData

    // convert the data into a quote
    val m = Map(pairs: _*)
    val (_52wLow, _52wHigh) = numberTuple(m.get("52wk Range:"), "[-]")
    val (ask, askSize) = numberTuple(m.get("Ask:"), "x")
    val (bid, bidSize) = numberTuple(m.get("Bid:"), "x")
    val (change, changePct) = numberTuple(trade.chg)
    val (daysLow, daysHigh) = numberTuple(m.get("Day's Range:"), "[-]")
    val (dividend, divYield) = numberTuple(m.get("Div & Yield:"))
    val prevClose = m.get("Prev Close:") flatMap asNumber

    // create the quote instance
    new YFRealtimeQuote(
      symbol.toUpperCase,
      title.name,
      title.exchange,
      trade.lastTrade flatMap asNumber,
      trade.tradeTime,
      trade.tradeTime flatMap asTime,
      change,
      changePct,
      prevClose,
      m.get("Open:") flatMap asNumber,
      toClose(prevClose, change),
      ask,
      askSize map (_.toInt),
      bid,
      bidSize map (_.toInt),
      m.get("1y Target Est:") flatMap asNumber,
      m.get("Beta:") flatMap asNumber,
      m.get("Next Earnings Date:") flatMap (asDate(_, "dd-MMM-yy")),
      daysLow,
      daysHigh,
      toSpread(daysHigh, daysLow),
      _52wLow,
      _52wHigh,
      m.get("Volume:") flatMap asLong,
      m.get("Avg Vol (3m):") flatMap asLong,
      m.get("Market Cap:") flatMap asNumber,
      m.get("P/E (ttm):") flatMap asNumber,
      m.get("EPS (ttm):") flatMap asNumber,
      dividend,
      divYield,
      if (m.isEmpty) Some("Quote not found") else None,
      responseTimeMsec)
  }

  /**
   * Parses the header portion of the document for the company name,
   * exchange, last trade, trade time, and change ($/%).
   */
  private def parseHeader(doc: NodeSeq): (YFTitle, YFTrade) = {

    def parseTitleInfo(div: Node): YFTitle = {
      val name = ((div \ "h2") map (_.text.trim) headOption) map parseCompanyName
      val exchange = ((div \\ "span") map (_.text.trim) headOption) map (_.replaceAll("-", ""))
      YFTitle(name, exchange)
    }

    def parseCompanyName(s: String): String = {
      val index = s.indexOf('(')
      if (index > 0) s.substring(0, index).trim else s
    }

    def grab(m: Map[String, Option[String]], key: String): Option[String] = m.get(key) flatten

    /**
     * <div class="yfi_rt_quote_summary_rt_top sigfig_promo_1">
     * <div>
     * <span class="time_rtq_ticker"><span id="yfs_l84_brk-a">190,705.00</span></span>
     * <span class="up_g time_rtq_content"><span id="yfs_c63_brk-a">
     * <img width="10" height="14" style="margin-right: -2px;" border="0"
     * src="http://l.yimg.com/os/mit/media/m/base/images/transparent-1093278.png"
     * class="pos_arrow" alt="Up"> 730.00</span><span id="yfs_p43_brk-a">(0.38%)</span>
     * </span>
     * <span class="time_rtq"> <span id="yfs_t53_brk-a">
     * <span id="yfs_t53_brk-a">2:43PM EDT</span></span></span>
     * </div>
     * </div>
     */
    def parseTradeInfo(div: Node): YFTrade = {
      // build a mapping of the span class names to values
      val m = Map((div \\ "span") flatMap { span =>
        val name = (span \ "@class").text.trim
        if (name.length == 0) Seq.empty
        else {
          val value = (span map (_.text.trim)).headOption
          if (name.contains(' ')) name.split("[ ]") map ((_, value)) toSeq else Seq((name, value))
        }
      }: _*)
      YFTrade(grab(m, "time_rtq_ticker"), grab(m, "time_rtq"), grab(m, "time_rtq_content"))
    }

    // gather the title and trade objects
    val data: Seq[YFDivData] = (doc \\ "div") flatMap { div =>
      // identify the parts we need by class
      val divClass = div \ "@class"
      if (divClass.exists(_.text == "title")) Some(parseTitleInfo(div))
      else if (divClass.exists(_.text.contains("yfi_rt_quote_summary_rt_top"))) Some(parseTradeInfo(div))
      else None
    }

    // return the tuple
    data match {
      case Seq(title: YFTitle, trade: YFTrade) => (title, trade)
      case Seq(title: YFTitle, _*) => (title, YFTrade(None, None, None))
      case Seq(trade: YFTrade, _*) => (YFTitle(None, None), trade)
      case _ => (YFTitle(None, None), YFTrade(None, None, None))
    }
  }

  private def parseTableData(table: NodeSeq): Seq[(String, String)] = {
    if ((table \\ "@id").exists(n => n.text == "table1" || n.text == "table2")) {
      val rows = table \\ "tr"
      rows map { row =>
        val label = (row \\ "th").text.trim
        val value = (row \\ "td").text.trim
        (label, value)
      }
    } else Seq.empty
  }

  sealed trait YFDivData

  case class YFTitle(name: Option[String], exchange: Option[String]) extends YFDivData

  case class YFTrade(lastTrade: Option[String], tradeTime: Option[String], chg: Option[String]) extends YFDivData

  /**
   * Represents the retrieved real-time quote
   * @author lawrence.daniels@gmail.com
   */
  class YFRealtimeQuote(val symbol: String,
                        val name: Option[String],
                        val exchange: Option[String],
                        val lastTrade: Option[Double],
                        val time: Option[String],
                        val tradeDateTime: Option[Date],
                        val change: Option[Double],
                        val changePct: Option[Double],
                        val prevClose: Option[Double],
                        val open: Option[Double],
                        val close: Option[Double],
                        val ask: Option[Double],
                        val askSize: Option[Int],
                        val bid: Option[Double],
                        val bidSize: Option[Int],
                        val target1Yr: Option[Double],
                        val beta: Option[Double],
                        val nextEarningsDate: Option[Date],
                        val low: Option[Double],
                        val high: Option[Double],
                        val spread: Option[Double],
                        val low52Week: Option[Double],
                        val high52Week: Option[Double],
                        val volume: Option[Long],
                        val avgVol3m: Option[Long],
                        val marketCap: Option[Double],
                        val peRatio: Option[Double],
                        val eps: Option[Double],
                        val dividend: Option[Double],
                        val divYield: Option[Double],
                        val error: Option[String],
                        val responseTimeMsec: Long)

}