package com.shocktrade.server.services

import com.shocktrade.server.services.BloombergQuoteService._
import io.scalajs.npm.request.Request
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Bloomberg Quote Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BloombergQuoteService() {
  private val scriptParser = new ScriptParser[BloombergQuote]()

  /**
    * Attempts to retrieve the quote for the given symbol
    * @param symbol the given symbol
    * @return the promise of the option of a [[BloombergQuote quote]] object
    */
  def apply(symbol: String)(implicit ec: ExecutionContext): Future[Option[BloombergQuote]] = {
    for {
      (response, html) <- Request.getAsync(s"http://www.bloomberg.com/quote/$symbol:US")
      quote_? <- scriptParser.parse(html, anchor = s""""/markets/api/quote-page/$symbol%3AUS?locale=en":""")
    } yield quote_?
  }

}

/**
  * Bloomberg Quote Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BloombergQuoteService {

  @js.native
  trait BloombergQuote extends js.Object {
    val basicQuote: js.UndefOr[BloombergBasicQuote] = js.native
    val boardMembers: js.UndefOr[BloombergBoardMembers] = js.native
    val chartDefaultTimeFrame: js.UndefOr[String] = js.native
    val companyNews: js.UndefOr[BloombergCompanyNews] = js.native
    val companyVideos: js.UndefOr[BloombergCompanyNews] = js.native
    val detailedQuote: js.UndefOr[BloombergDetailedQuote] = js.native
    val executives: js.UndefOr[BloombergExecutives] = js.native
    val marketStatus: js.UndefOr[BloombergDetailedMarketStatus] = js.native
    val priceTimeSeries: js.UndefOr[BloombergPriceTimeSeries] = js.native
    val pressReleases: js.UndefOr[BloombergPressRelease] = js.native
    val profile: js.UndefOr[BloombergProfile] = js.native
    val securityType: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergBoardMembers extends js.Object {
    val id: js.UndefOr[String] = js.native
    val boardMembers: js.UndefOr[js.Array[js.Object]] = js.native
    val count: js.UndefOr[Int] = js.native
    val locale: js.UndefOr[String] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergBasicQuote extends js.Object {
    val id: js.UndefOr[String] = js.native
    val name: js.UndefOr[String] = js.native
    val primaryExchange: js.UndefOr[String] = js.native
    val price: js.UndefOr[Double] = js.native
    val issuedCurrency: js.UndefOr[String] = js.native
    val priceChange1Day: js.UndefOr[Double] = js.native
    val percentChange1Day: js.UndefOr[Double] = js.native
    val priceMinDecimals: js.UndefOr[Int] = js.native
    val nyTradeStartTime: js.UndefOr[String] = js.native
    val nyTradeEndTime: js.UndefOr[String] = js.native
    val timeZoneOffset: js.UndefOr[Int] = js.native
    val lastUpdateEpoch: js.UndefOr[Double] = js.native
    val openPrice: js.UndefOr[Double] = js.native
    val lowPrice: js.UndefOr[Double] = js.native
    val highPrice: js.UndefOr[Double] = js.native
    val volume: js.UndefOr[Double] = js.native
    val previousClosingPriceOneTradingDayAgo: js.UndefOr[Int] = js.native
    val lowPrice52Week: js.UndefOr[Double] = js.native
    val highPrice52Week: js.UndefOr[Double] = js.native
    val totalReturn1Year: js.UndefOr[Double] = js.native
    val priceDate: js.UndefOr[String] = js.native
    val priceTime: js.UndefOr[String] = js.native
    val lastUpdateTime: js.UndefOr[String] = js.native
    val lastUpdateISO: js.UndefOr[String] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergDetailedQuote extends js.Object {
    val id: js.UndefOr[String] = js.native
    val priceEarningsRatio: js.UndefOr[Double] = js.native
    val earningsPerShare: js.UndefOr[Double] = js.native
    val priceMinDecimals: js.UndefOr[Double] = js.native
    val fundamentalDataCurrency: js.UndefOr[String] = js.native
    val marketCap: js.UndefOr[Double] = js.native
    val issuedCurrency: js.UndefOr[String] = js.native
    val sharesOutstanding: js.UndefOr[Double] = js.native
    val priceToSalesRatio: js.UndefOr[Double] = js.native
    val indicatedGrossDividendYield: js.UndefOr[Double] = js.native
    val bicsSector: js.UndefOr[String] = js.native
    val bicsIndustry: js.UndefOr[String] = js.native
    val bicsSubIndustry: js.UndefOr[String] = js.native
    val openPrice: js.UndefOr[Double] = js.native
    val lowPrice: js.UndefOr[Double] = js.native
    val highPrice: js.UndefOr[Double] = js.native
    val volume: js.UndefOr[Double] = js.native
    val previousClosingPriceOneTradingDayAgo: js.UndefOr[Double] = js.native
    val lowPrice52Week: js.UndefOr[Double] = js.native
    val highPrice52Week: js.UndefOr[Double] = js.native
    val totalReturn1Year: js.UndefOr[Double] = js.native
    val totalReturnYtd: js.UndefOr[Double] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
    val locale: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergDetailedMarketStatus extends js.Object {
    val id: js.UndefOr[String] = js.native
    val marketStatus: js.UndefOr[String] = js.native
    val parentMarketStatus: js.UndefOr[String] = js.native
    val ultimateParentTicker: js.UndefOr[String] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergExecutive extends js.Object {
    val id: js.UndefOr[String] = js.native
    val name: js.UndefOr[String] = js.native
    val title: js.UndefOr[String] = js.native
    val slug: js.UndefOr[String] = js.native
    val linkToProfilePage: js.UndefOr[Boolean] = js.native
  }

  @js.native
  trait BloombergExecutives extends js.Object {
    val id: js.UndefOr[String] = js.native
    val executives: js.UndefOr[js.Array[BloombergExecutive]] = js.native
    val count: js.UndefOr[Int] = js.native
    val locale: js.UndefOr[String] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergPriceTimeSeries extends js.Object {
    val id: js.UndefOr[String] = js.native
    val dateTimeRanges: js.UndefOr[BloombergDateTimeRanges] = js.native
    val price: js.UndefOr[js.Array[BloombergPrice]] = js.native
    val previousClosingPriceOneTradingDayAgo: js.UndefOr[Double] = js.native
    val tradingDayCloseUTC: js.UndefOr[Double] = js.native
    val lastUpdateUTC: js.UndefOr[Double] = js.native
    val timeZoneOffset: js.UndefOr[Double] = js.native
    val nyTradeStartTime: js.UndefOr[String] = js.native
    val nyTradeEndTime: js.UndefOr[String] = js.native
    val priceMinDecimals: js.UndefOr[Double] = js.native
    val lastUpdateDate: js.UndefOr[String] = js.native
    val lastPrice: js.UndefOr[Double] = js.native
  }

  @js.native
  trait BloombergDateTimeRanges extends js.Object {
    val start: js.UndefOr[js.Date] = js.native
    val end: js.UndefOr[js.Date] = js.native
  }

  @js.native
  trait BloombergPrice extends js.Object {
    val dateTime: js.UndefOr[js.Date] = js.native
    val value: js.UndefOr[Double] = js.native
  }

  @js.native
  trait BloombergCompanyNews extends js.Object {
    val id: js.UndefOr[String] = js.native
    val news: js.UndefOr[js.Array[BloombergNewsItem]] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
  }

  @js.native
  trait BloombergNewsItem extends js.Object {
    val id: js.UndefOr[String] = js.native
    val headline: js.UndefOr[String] = js.native
    val publishedAt: js.UndefOr[String] = js.native
    val url: js.UndefOr[String] = js.native
    val primaryCategory: js.UndefOr[String] = js.native
    val provider: js.UndefOr[String] = js.native
    val providerLabel: js.UndefOr[String] = js.native
    val publishedAtISO: js.UndefOr[js.Date] = js.native
    val personalizationTrackingMetaData: js.UndefOr[String] = js.native
    val tickerData: js.UndefOr[String] = js.native
    val tickers: js.UndefOr[js.Array[String]] = js.native
  }

  @js.native
  trait BloombergPressRelease extends js.Object {
    val pressReleases: js.UndefOr[js.Array[BloombergNewsItem]] = js.native
  }

  @js.native
  trait BloombergProfile extends js.Object {
    val id: js.UndefOr[String] = js.native
    val description: js.UndefOr[String] = js.native
    val address: js.UndefOr[js.Array[String]] = js.native
    val phone: js.UndefOr[String] = js.native
    val website: js.UndefOr[String] = js.native
    val userTimeZone: js.UndefOr[String] = js.native
    val locale: js.UndefOr[String] = js.native
  }

}