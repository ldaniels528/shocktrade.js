package com.shocktrade.controllers

import com.shocktrade.models.profile.{Condition, Filter}
import com.shocktrade.services.yahoofinance.YFRealtimeStockQuoteService
import play.api.Logger
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Quote Filtering Capability
 * @author lawrence.daniels@gmail.com
 */
trait QuoteFiltering {

  def createQueryJs(filter: Filter): JsObject = {

    def toJson(c: Condition): JsObject = {
      c.operator match {
        case "=" => JS(c.field -> c.value)
        case "!=" => JS(c.field -> JS("$ne" -> c.value))
        case "<" => JS(c.field -> JS("$lt" -> c.value))
        case ">" => JS(c.field -> JS("$gt" -> c.value))
        case "<=" => JS(c.field -> JS("$lte" -> c.value))
        case ">=" => JS(c.field -> JS("$gte" -> c.value))
        case _ =>
          Logger.warn(s"Unhandled condition: [${c.field} ${c.operator} ${c.value}]")
          JS()
      }
    }

    filter.conditions.foldLeft[JsObject](JS("active" -> true)) { (res, c) => res ++ toJson(c) }
  }

  def createQueryJs(filter: Filter, exchanges: Seq[String] = Nil): JsObject = {
    createQueryJs(filter) ++ (if (exchanges.nonEmpty) JS("exchange" -> JS("$in" -> exchanges)) else JS())
  }

  def findRealtimeQuote(symbol: String)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      q <- YFRealtimeStockQuoteService.getQuote(symbol)
      js = Json.obj(
        "symbol" -> q.symbol, "name" -> q.name, "exchange" -> q.exchange, "lastTrade" -> q.lastTrade,
        "time" -> q.time, "tradeDateTime" -> q.tradeDateTime, "change" -> q.change,
        "changePct" -> q.changePct, "prevClose" -> q.prevClose, "open" -> q.open, "close" -> q.close,
        "ask" -> q.ask, "askSize" -> q.askSize, "bid" -> q.bid, "bidSize" -> q.bidSize,
        "target1Yr" -> q.target1Yr, "beta" -> q.beta, "nextEarningsDate" -> q.nextEarningsDate,
        "low" -> q.low, "high" -> q.high, "spread" -> q.spread, "low52Week" -> q.low52Week,
        "high52Week" -> q.high52Week, "volume" -> q.volume, "avgVol3m" -> q.avgVol3m,
        "marketCap" -> q.marketCap, "peRatio" -> q.peRatio, "eps" -> q.eps, "dividend" -> q.dividend,
        "divYield" -> q.divYield, "responseTimeMsec" -> q.responseTimeMsec)
    } yield js
  }

  def sortJs(f: Filter): JsObject = {
    val direction = if (f.ascending.contains(true)) 1 else -1
    JS(f.sortField.get -> direction)
  }

}