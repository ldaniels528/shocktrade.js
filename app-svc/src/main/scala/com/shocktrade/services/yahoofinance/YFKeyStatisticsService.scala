package com.shocktrade.services.yahoofinance

import java.util.{Calendar, Date}

import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

/**
 * Yahoo! Finance: Key Statistics Service
 * @author lawrence.daniels@gmail.com
 */
object YFKeyStatisticsService extends YFWebService {

  /**
   * Retrieves the key statistics for the given option symbol
   * @param symbol the given symbol (e.g. "AAPL")
   * @return a { @link Future future} of a { @link YFOptionQuote quote}
   */
  def getKeyStatisticsSync(symbol: String): YFKeyStatistics = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve the document
    val doc =
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q/ks?s=$symbol+Key+Statistics")

    // parse the document
    parseDocument(symbol, doc, startTime)
  }

  /**
   * Retrieves the key statistics for the given option symbol
   * @param symbol the given symbol (e.g. "AAPL")
   * @return a { @link Future future} of a { @link YFOptionQuote quote}
   */
  def getKeyStatistics(symbol: String)(implicit ec: ExecutionContext): Future[YFKeyStatistics] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q/ks?s=$symbol+Key+Statistics")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Parses the document; transforming its contents into a key statistics object
   */
  private def parseDocument(symbol: String, doc: NodeSeq, startTime: Long): YFKeyStatistics = {
    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse the result
    val tuples = doc \\ "table" flatMap { table =>
      if ((table \\ "@class").exists(_.text == "yfnc_datamodoutline1")) {
        table \ "tr" \ "td" \ "table" \ "tr" flatMap { rows =>
          (rows \ "td") map (_.text.trim) match {
            case Seq(a, b, _*) => Some((a, b))
            case _ => None
          }
        }
      } else Seq.empty
    }

    toKeyStatistics(symbol, Map(tuples: _*), responseTimeMsec)
  }

  /**
   * Transforms the given data mapping to a key statistics object
   */
  private def toKeyStatistics(symbol: String, m: Map[String, String], responseTimeMsec: Long) = {
    //m.toSeq sortBy (_._1) foreach { case (k, v) => println(s"$k -> $v") }
    new YFKeyStatistics(
      symbol,
      m ~> "% Held by Insiders" flatMap asNumber,
      m ~> "% Held by Institutions" flatMap asNumber,
      m ~> "5 Year Average Dividend Yield" flatMap asNumber,
      m ~> "52-Week Change" flatMap asNumber,
      m ~> "52-Week High" flatMap asNumber,
      m ~> "52-Week Low" flatMap asNumber,
      m ~> "50-Day Moving Average" flatMap asNumber,
      m ~> "200-Day Moving Average" flatMap asNumber,
      m ~> "Avg Vol (3 month)" flatMap asLong,
      m ~> "Avg Vol (10 day)" flatMap asLong,
      m.get("Beta:") flatMap asNumber,
      m.get("Book Value Per Share (mrq):") flatMap asNumber,
      m.get("Current Ratio (mrq):") flatMap asNumber,
      m.get("Diluted EPS (ttm):") flatMap asNumber,
      m ~> "Dividend Date" flatMap toDate_MMM_dd_yyyy,
      m ~> "EBITDA (ttm)" flatMap asNumber,
      m ~> "Enterprise Value (" flatMap asNumber,
      m ~> "Enterprise Value/EBITDA (ttm)" flatMap asNumber,
      m ~> "Enterprise Value/Revenue (ttm)" flatMap asNumber,
      m ~> "Ex-Dividend Date" flatMap toDate_MMM_dd_yyyy,
      m.get("Fiscal Year Ends:") flatMap toDate_MMM_DD,
      m.get("Float:") flatMap asLong,
      m ~> "Forward Annual Dividend Rate" flatMap asNumber,
      m ~> "Forward Annual Dividend Yield" flatMap asNumber,
      m ~> "Forward P/E" flatMap asNumber,
      m.get("Gross Profit (ttm):") flatMap asNumber,
      m ~> "Last Split Date" flatMap toDate_MMM_dd_yyyy,
      m ~> "Last Split Factor" flatMap cleanse,
      m.get("Levered Free Cash Flow (ttm):") flatMap asNumber,
      m ~> "Market Cap (intraday)" flatMap asNumber,
      m.get("Most Recent Quarter (mrq):") flatMap toDate_MMM_dd_yyyy,
      m.get("Net Income Avl to Common (ttm):") flatMap asNumber,
      m.get("Operating Cash Flow (ttm):") flatMap asNumber,
      m.get("Operating Margin (ttm):") flatMap asNumber,
      m ~> "PEG Ratio (5 yr expected)" flatMap asNumber,
      m.get("Payout Ratio4:") flatMap asNumber,
      m.get("Price/Book (mrq):") flatMap asNumber,
      m.get("Price/Sales (ttm):") flatMap asNumber,
      m.get("Profit Margin (ttm):") flatMap asNumber,
      m.get("Qtrly Earnings Growth (yoy):") flatMap asNumber,
      m.get("Qtrly Revenue Growth (yoy):") flatMap asNumber,
      m.get("Return on Assets (ttm):") flatMap asNumber,
      m.get("Return on Equity (ttm):") flatMap asNumber,
      m.get("Revenue (ttm):") flatMap asNumber,
      m.get("Revenue Per Share (ttm):") flatMap asNumber,
      m ~> "S&P500 52-Week Change" flatMap asNumber,
      m ~> "Shares Outstanding" flatMap asLong,
      m ~> "Shares Short (as of" flatMap asLong,
      m ~> "Shares Short (prior month)" flatMap asLong,
      m ~> "Short % of Float" flatMap asNumber,
      m ~> "Short Ratio" flatMap asNumber,
      m.get("Total Cash (mrq):") flatMap asNumber,
      m.get("Total Cash Per Share (mrq):") flatMap asNumber,
      m.get("Total Debt (mrq):") flatMap asNumber,
      m.get("Total Debt/Equity (mrq):") flatMap asNumber,
      m ~> "Trailing Annual Dividend Yield" flatMap asNumber,
      m.get("Trailing P/E (ttm, intraday):") flatMap asNumber,
      responseTimeMsec)
  }

  private def toDate_MMM_dd_yyyy(s: String): Option[Date] = asDate(s, "MMM dd, yyyy")

  private def toDate_MMM_DD(s: String): Option[Date] = {
    asDate(s, "MMM dd") map { date =>
      val cal = Calendar.getInstance()
      val year = cal.get(Calendar.YEAR)
      new DateTime(date).withYear(year).toDate
    }
  }

  /**
   * Represents the key statistics data
   * @author lawrence.daniels@gmail.com
   */
  class YFKeyStatistics(val symbol: String,
                        val pctHeldByInsiders: Option[Double],
                        val pctHeldByInstitutions: Option[Double],
                        val dividendYield5YearAvg: Option[Double],
                        val change52Week: Option[Double],
                        val high52Week: Option[Double],
                        val low52Week: Option[Double],
                        val movingAverage50Day: Option[Double],
                        val movingAverage200Day: Option[Double],
                        val avgVolume3Month: Option[Long],
                        val avgVolume10Day: Option[Long],
                        val beta: Option[Double],
                        val bookValuePerShare: Option[Double],
                        val currentRatio: Option[Double],
                        val dilutedEPS: Option[Double],
                        val dividendDate: Option[Date],
                        val EBITDA: Option[Double],
                        val enterpriseValue: Option[Double],
                        val enterpriseValueOverEBITDA: Option[Double],
                        val enterpriseValueOverRevenue: Option[Double],
                        val exDividendDate: Option[Date],
                        val fiscalYearEndDate: Option[Date],
                        val sharesFloat: Option[Long],
                        val forwardAnnualDividendRate: Option[Double],
                        val forwardAnnualDividendYield: Option[Double],
                        val forwardPE: Option[Double],
                        val grossProfit: Option[Double],
                        val lastSplitDate: Option[Date],
                        val lastSplitFactor: Option[String],
                        val leveredFreeCashFlow: Option[Double],
                        val marketCapIntraday: Option[Double],
                        val mostRecentQuarterDate: Option[Date],
                        val netIncomeAvailToCommon: Option[Double],
                        val operatingCashFlow: Option[Double],
                        val operatingMargin: Option[Double],
                        val pegRatio5YearExp: Option[Double],
                        val payoutRatio: Option[Double],
                        val priceOverBookValue: Option[Double],
                        val priceOverSales: Option[Double],
                        val profitMargin: Option[Double],
                        val earningsGrowthQuarterly: Option[Double],
                        val revenueGrowthQuarterly: Option[Double],
                        val returnOnAssets: Option[Double],
                        val returnOnEquity: Option[Double],
                        val revenue: Option[Double],
                        val revenuePerShare: Option[Double],
                        val change52WeekSNP500: Option[Double],
                        val sharesOutstanding: Option[Long],
                        val sharesShort: Option[Long],
                        val sharesShortPriorMonth: Option[Long],
                        val shortPctOfFloat: Option[Double],
                        val shortRatio: Option[Double],
                        val totalCash: Option[Double],
                        val totalCashPerShare: Option[Double],
                        val totalDebt: Option[Double],
                        val totalDebtOverEquity: Option[Double],
                        val trailingAnnualDividendYield: Option[Double],
                        val trailingPE: Option[Double],
                        val responseTimeMsec: Long)

}