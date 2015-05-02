package com.shocktrade.controllers

import com.shocktrade.services.yahoofinance.YFRealtimeStockQuoteService
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Quote Filtering Capability
 * @author lawrence.daniels@gmail.com
 */
trait QuoteFiltering {

  def createQueryJs(filter: Filter): JsObject = {

    def toJson(c: Condition): JsObject = {
      val field = toField(c.field)
      val value = c.value //toValue(c.field, c.value)
      c.operator match {
        case "=" => JS(field -> value)
        case "!=" => JS(field -> JS("$ne" -> value))
        case "<" => JS(field -> JS("$lt" -> value))
        case ">" => JS(field -> JS("$gt" -> value))
        case "<=" => JS(field -> JS("$lte" -> value))
        case ">=" => JS(field -> JS("$gte" -> value))
        case _ =>
          Logger.warn(s"Unhandled condition: [$field ${c.operator} $value]")
          JS()
      }
    }

    filter.conditions.foldLeft[JsObject](JS("active" -> true)) { (res, c) => res ++ toJson(c) }
  }

  def createQueryJs(filter: Filter, exchanges: Seq[String]): JsObject = {
    createQueryJs(filter) ++ JS("exchange" -> JS("$in" -> exchanges))
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
    val direction = if (f.ascending) 1 else -1
    JS(toField(f.sortField) -> direction)
  }

  private def toField(field: String) = {
    field match {
      case "CHANGE" => "changePct"
      case "CHANGEPCT" => "changePct"
      case _ => field.toLowerCase
    }
  }

  private def toValue(field: String, value: String) = value.toDouble

  implicit val conditionReads: Reads[Condition] = (
    (JsPath \ "field").read[String] and
      (JsPath \ "operator").read[String] and
      (JsPath \ "value").read[Double])(Condition.apply _)

  implicit val filterReads: Reads[Filter] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "sortField").read[String] and
      (JsPath \ "ascending").read[Boolean] and
      (JsPath \ "maxResults").read[Int] and
      (JsPath \ "conditions").read[Seq[Condition]])(Filter.apply _)

  case class Filter(
                     name: String,
                     sortField: String,
                     ascending: Boolean,
                     maxResults: Int,
                     conditions: Seq[Condition])

  case class Condition(field: String, operator: String, value: Double)

}