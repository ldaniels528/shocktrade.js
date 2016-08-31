package com.shocktrade.server.services

import com.shocktrade.server.services.NASDAQIntraDayQuotesService._
import org.scalajs.nodejs.htmlparser2.{HtmlParser2, ParserHandler, ParserOptions}
import org.scalajs.nodejs.request.Request
import org.scalajs.nodejs.{NodeRequire, console, errors}

import scala.concurrent.{ExecutionContext, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.runtime._
import scala.util.{Failure, Success, Try}

/**
  * NASDAQ Intra-day Quotes Service
  * @author lawrence.daniels@gmail.com
  */
class NASDAQIntraDayQuotesService()(implicit require: NodeRequire) {
  private val htmlParser = HtmlParser2()
  private val request = Request()

  def getQuotes(symbol: String, timeIndex: Int = 0, pageNo: Int = 1)(implicit ec: ExecutionContext) = {
    val startTime = js.Date.now()
    val promise = Promise[NASDAQIntraDayQuotes]()
    request.getFuture(toURL(symbol, timeIndex, pageNo)) foreach { case (response, html) =>
      val parser = htmlParser.Parser(new ParserHandler {
        val sb = new StringBuilder()
        val headers = js.Array[String]()
        val rows = js.Array[js.Dictionary[String]]()
        var columns = js.Dictionary[String]()
        var inDiv: Boolean = false
        var inTable: Boolean = false

        override def onopentag = (tag: String, attributes: js.Dictionary[String]) => {
          if (!inDiv && tag == "div" && attributes.get("id").contains("quotes_content_left__panelTradeData")) inDiv = true
          else if (inDiv && tag == "table") inTable = true
        }

        override def onclosetag = (tag: String) => {
          if (inDiv && inTable) {
            tag match {
              case "th" =>
                headers.append(sb.toString().trim)
              case "td" =>
                columns(headers(columns.size)) = sb.toString().trim
              case "tr" =>
                rows.append(columns)
                columns = js.Dictionary[String]()
              case "table" =>
                inTable = false
                inDiv = false
              case _ =>
            }
          }
          sb.clear()
        }

        override def ontext = (text: String) => sb.append(text)

        override def onend = () => promise.success(toQuotes(symbol, headers, rows, startTime))

        override def onerror = (err: errors.Error) => promise.failure(wrapJavaScriptException(err))

      }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

      parser.write(html)
      parser.end()
    }
    promise.future
  }

  @inline
  private def toQuotes(symbol: String, headers: js.Array[String], rows: js.Array[js.Dictionary[String]], startTime: Double) = {
    new NASDAQIntraDayQuotes(symbol = symbol, responseTimeMsec = js.Date.now() - startTime, quotes = rows.tail map (row =>
      new NASDAQIntraDayQuote(
        price = row.get("NLS Price").orUndefined flatMap toNumber,
        tradeTime = row.get("NLS Time (ET)") orUndefined,
        volume = row.get("NLS Share Volume").orUndefined flatMap toNumber
      )))
  }

  @inline
  private def toURL(symbol: String, timeIndex: Int, pageNo: Int) = {
    s"http://www.nasdaq.com/symbol/${symbol.toLowerCase}/time-sales?time=${timeSlots(timeIndex)}&pageNo=$pageNo"
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
  * @author lawrence.daniels@gmail.com
  */
object NASDAQIntraDayQuotesService {

  // time slot constants
  private val timeSlots = Seq(
    "ET_0930_TO_0959", "ET_1000_TO_1029", "ET_1030_TO_1059", "ET_1100_TO_1129",
    "ET_1130_TO_1159", "ET_1200_TO_1229", "ET_1230_TO_1259", "ET_1300_TO_1329",
    "ET_1330_TO_1359", "ET_1400_TO_1429", "ET_1430_TO_1459", "ET_1500_TO_1529",
    "ET_1530_TO_1600"
  )

  @ScalaJSDefined
  class NASDAQIntraDayQuotes(val symbol: String,
                             val quotes: js.Array[NASDAQIntraDayQuote],
                             val responseTimeMsec: Double) extends js.Object

  @ScalaJSDefined
  class NASDAQIntraDayQuote(val price: js.UndefOr[Double],
                            val tradeTime: js.UndefOr[String],
                            val volume: js.UndefOr[Double]) extends js.Object

}