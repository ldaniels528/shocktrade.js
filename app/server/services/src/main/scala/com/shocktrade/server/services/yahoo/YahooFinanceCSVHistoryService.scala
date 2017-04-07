package com.shocktrade.server.services.yahoo

import com.shocktrade.server.services.yahoo.YahooFinanceCSVHistoryService._
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.request.Request
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Yahoo Finance! CSV History Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceCSVHistoryService() {

  def apply(symbol: String, from: js.Date, to: js.Date)(implicit ec: ExecutionContext): Future[YFHistoricalQuotes] = {
    val startTime = js.Date.now()
    Request.getAsync(toURL(symbol, from, to)) map { case (response, data) =>
      new YFHistoricalQuotes(symbol = symbol, quotes = parseHistory(data), responseTime = js.Date.now() - startTime)
    }
  }

  private def parseHistory(data: String) = {
    data.split("[\n]") flatMap { line =>
      line.split("[,]") match {
        case Array(date, open, high, low, close, volume, adjClose) if date != "Date" =>
          Option(new YFHistoricalQuote(
            tradeDate = Moment(date).toDate(),
            open = Try(open.toDouble).toOption.orUndefined,
            high = Try(high.toDouble).toOption.orUndefined,
            low = Try(low.toDouble).toOption.orUndefined,
            close = Try(close.toDouble).toOption.orUndefined,
            volume = Try(volume.toDouble).toOption.orUndefined,
            adjClose = Try(adjClose.toDouble).toOption.orUndefined
          ))
        case _ => None
      }
    } toJSArray
  }

  private def toURL(symbol: String, from: js.Date, to: js.Date) = {
    val (m0, d0, y0) = (from.getMonth(), from.getDay(), from.getFullYear())
    val (m1, d1, y1) = (to.getMonth(), to.getDay(), to.getFullYear())
    s"http://chart.finance.yahoo.com/table.csv?s=$symbol&a=$m0&b=$d0&c=$y0&d=$m1&e=$d1&f=$y1&g=d&ignore=.csv"
  }

}

/**
  * Yahoo Finance! CSV History Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceCSVHistoryService {

  @ScalaJSDefined
  class YFHistoricalQuotes(val symbol: String,
                           val quotes: js.Array[YFHistoricalQuote],
                           val responseTime: Double) extends js.Object

  @ScalaJSDefined
  class YFHistoricalQuote(val tradeDate: js.UndefOr[js.Date],
                          val open: js.UndefOr[Double],
                          val high: js.UndefOr[Double],
                          val low: js.UndefOr[Double],
                          val close: js.UndefOr[Double],
                          val volume: js.UndefOr[Double],
                          val adjClose: js.UndefOr[Double]) extends js.Object

}
