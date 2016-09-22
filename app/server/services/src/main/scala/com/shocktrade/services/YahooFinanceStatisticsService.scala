package com.shocktrade.services

import com.shocktrade.services.YahooFinanceStatisticsService._
import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.htmlparser2.{HtmlParser2, ParserHandler, ParserOptions}
import org.scalajs.nodejs.request.Request
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, console, errors}

import scala.concurrent.{ExecutionContext, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.runtime._
import scala.util.{Failure, Success, Try}

/**
  * Yahoo Finance! Statistics Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceStatisticsService()(implicit require: NodeRequire) {
  private val htmlParser = HtmlParser2()
  private val request = Request()

  /**
    * Attempts to retrieve the statistics for the given symbol
    * @param symbol the given symbol
    * @return the promise of the option of a [[YFKeyStatistics key statistics]] object
    */
  def apply(symbol: String)(implicit ec: ExecutionContext) = {
    val promise = Promise[Option[YFKeyStatistics]]()
    request.getFuture(s"https://finance.yahoo.com/quote/$symbol/key-statistics") onComplete {
      case Success((response, html)) =>
        val parser = htmlParser.Parser(new ParserHandler {
          val sb = new StringBuilder()
          val quotes = js.Array[YFKeyStatistics]()

          override def onclosetag = (tag: String) => {
            if (tag == "script") parseKeyStatistics(sb.toString()) foreach (quotes.push(_))
            sb.clear()
          }

          override def ontext = (text: String) => sb.append(text)

          override def onend = () => promise.success(quotes.headOption)

          override def onerror = (err: errors.Error) => promise.failure(wrapJavaScriptException(err))

        }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

        parser.write(html)
        parser.end()
      case Failure(e) => promise.failure(e)
    }
    promise
  }

  @inline
  private def parseKeyStatistics(rawText: String) = {
    val anchor = "\"QuoteSummaryStore\":"
    for {
      index <- rawText.indexOfOpt(anchor)
      text = rawText.substring(index + anchor.length)
      limit <- findEndOfJsonBlock(text)
      jsonString = text.take(limit)
      statistics <- Try(JSON.parse(jsonString).asInstanceOf[YFKeyStatistics]) match {
        case Success(qss) => Option(qss)
        case Failure(e) =>
          console.error(s"parseScript: Error occurred: ${e.getMessage}")
          None
      }
    } yield statistics
  }

  @inline
  private def findEndOfJsonBlock(text: String): Option[Int] = {
    var pos = 0
    var level = 0
    val ca = text.toCharArray
    do {
      ca(pos) match {
        case '{' => level += 1
        case '}' => level -= 1
        case ch =>
      }
      pos += 1
    } while (pos < ca.length && level > 0)

    if (level == 0) Some(pos) else None
  }

}

/**
  * Yahoo Finance! Statistics Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceStatisticsService {

  /**
    * Represents the Yahoo! Finance Quote Summary Store object
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @js.native
  trait YFKeyStatistics extends js.Object {
    var symbol: js.UndefOr[String] = js.native
    var price: js.UndefOr[YFPriceType] = js.native
    var quoteType: js.UndefOr[YFQuoteType] = js.native
    var summaryDetail: js.UndefOr[YFSummaryDetail] = js.native
  }

  @js.native
  trait YFPriceType extends js.Object {
    var averageDailyVolume10Day: js.UndefOr[YFQuantityType] = js.native
    var averageDailyVolume3Month: js.UndefOr[YFQuantityType] = js.native
    var currency: js.UndefOr[String] = js.native
    var currencySymbol: js.UndefOr[String] = js.native
    var exchange: js.UndefOr[String] = js.native
    var exchangeName: js.UndefOr[String] = js.native
    var longName: js.UndefOr[String] = js.native
    var marketState: js.UndefOr[String] = js.native
    var maxAge: js.UndefOr[Double] = js.native
    var openInterest: js.UndefOr[YFQuantityType] = js.native
    var postMarketChange: js.UndefOr[YFQuantityType] = js.native
    var postMarketChangePercent: js.UndefOr[YFQuantityType] = js.native
    var postMarketPrice: js.UndefOr[YFQuantityType] = js.native
    var postMarketSource: js.UndefOr[String] = js.native
    var postMarketTime: js.UndefOr[Double] = js.native
    var preMarketChange: js.UndefOr[YFQuantityType] = js.native
    var preMarketPrice: js.UndefOr[YFQuantityType] = js.native
    var preMarketSource: js.UndefOr[String] = js.native
    var quoteType: js.UndefOr[String] = js.native
    var regularMarketChange: js.UndefOr[YFQuantityType] = js.native
    var regularMarketChangePercent: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayHigh: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayLow: js.UndefOr[YFQuantityType] = js.native
    var regularMarketOpen: js.UndefOr[YFQuantityType] = js.native
    var regularMarketPreviousClose: js.UndefOr[YFQuantityType] = js.native
    var regularMarketSource: js.UndefOr[String] = js.native
    var regularMarketTime: js.UndefOr[Double] = js.native
    var regularMarketVolume: js.UndefOr[YFQuantityType] = js.native
    var shortName: js.UndefOr[String] = js.native
    var strikePrice: js.UndefOr[YFQuantityType] = js.native
    var symbol: js.UndefOr[String] = js.native
    var underlyingSymbol: js.UndefOr[String] = js.native
  }

  @js.native
  trait YFQuoteType extends js.Object {
    var uuid: js.UndefOr[String] = js.native
    var symbol: js.UndefOr[String] = js.native
    var exchange: js.UndefOr[String] = js.native
    var shortName: js.UndefOr[String] = js.native
    var longName: js.UndefOr[String] = js.native
    var headSymbol: js.UndefOr[String] = js.native
    var underlyingSymbol: js.UndefOr[String] = js.native
    var underlyingExchangeSymbol: js.UndefOr[String] = js.native
    var market: js.UndefOr[String] = js.native
    var quoteType: js.UndefOr[String] = js.native
    var messageBoardId: js.UndefOr[String] = js.native
  }

  @js.native
  trait YFSummaryDetail extends js.Object {
    var ask: js.UndefOr[YFQuantityType] = js.native
    var askSize: js.UndefOr[YFQuantityType] = js.native
    var averageDailyVolume10Day: js.UndefOr[YFQuantityType] = js.native
    var averageVolume: js.UndefOr[YFQuantityType] = js.native
    var averageVolume10days: js.UndefOr[YFQuantityType] = js.native
    var beta: js.UndefOr[YFQuantityType] = js.native
    var bid: js.UndefOr[YFQuantityType] = js.native
    var bidSize: js.UndefOr[YFQuantityType] = js.native
    var dayHigh: js.UndefOr[YFQuantityType] = js.native
    var dayLow: js.UndefOr[YFQuantityType] = js.native
    var dividendRate: js.UndefOr[YFQuantityType] = js.native
    var dividendYield: js.UndefOr[YFQuantityType] = js.native
    var exDividendDate: js.UndefOr[YFQuantityType] = js.native
    var expireDate: js.UndefOr[YFQuantityType] = js.native
    var fiftyDayAverage: js.UndefOr[YFQuantityType] = js.native
    var fiftyTwoWeekHigh: js.UndefOr[YFQuantityType] = js.native
    var fiftyTwoWeekLow: js.UndefOr[YFQuantityType] = js.native
    var fiveYearAvgDividendYield: js.UndefOr[YFQuantityType] = js.native
    var forwardPE: js.UndefOr[YFQuantityType] = js.native
    var marketCap: js.UndefOr[YFQuantityType] = js.native
    var maxAge: js.UndefOr[Int] = js.native
    var navPrice: js.UndefOr[YFQuantityType] = js.native
    var openInterest: js.UndefOr[YFQuantityType] = js.native
    var previousClose: js.UndefOr[YFQuantityType] = js.native
    var priceToSalesTrailing12Months: js.UndefOr[YFQuantityType] = js.native
    var regularMarketDayLow: js.UndefOr[YFQuantityType] = js.native
    var regularMarketOpen: js.UndefOr[YFQuantityType] = js.native
    var regularMarketPreviousClose: js.UndefOr[YFQuantityType] = js.native
    var regularMarketVolume: js.UndefOr[YFQuantityType] = js.native
    var strikePrice: js.UndefOr[YFQuantityType] = js.native
    var totalAssets: js.UndefOr[YFQuantityType] = js.native
    var trailingAnnualDividendRate: js.UndefOr[YFQuantityType] = js.native
    var trailingAnnualDividendYield: js.UndefOr[YFQuantityType] = js.native
    var trailingPE: js.UndefOr[YFQuantityType] = js.native
    var twoHundredDayAverage: js.UndefOr[YFQuantityType] = js.native
    var volume: js.UndefOr[YFQuantityType] = js.native
    var `yield`: js.UndefOr[YFQuantityType] = js.native
    var ytdReturn: js.UndefOr[YFQuantityType] = js.native
  }

  @js.native
  trait YFQuantityType extends js.Object {
    var raw: js.UndefOr[Double] = js.native
    var fmt: js.UndefOr[String] = js.native
    var longFmt: js.UndefOr[String] = js.native
  }

}