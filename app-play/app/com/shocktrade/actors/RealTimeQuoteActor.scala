package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.QuoteMessages._
import com.shocktrade.services.yahoofinance.YFRealtimeStockQuoteService
import play.api.libs.json.JsObject
import play.api.libs.json.Json.{obj => JS, _}

/**
 * Real-time Stock Quote Actor
 * @author lawrence.daniels@gmail.com
 */
class RealTimeQuoteActor() extends Actor with ActorLogging {
  override def receive = {
    case GetQuote(symbol) =>
      sender ! getQuoteFromService(symbol)

    case GetQuotes(symbols) =>
      sender ! (symbols flatMap getQuoteFromService)

    case message =>
      unhandled(message)
  }

  private def getQuoteFromService(symbol: String): Option[JsObject] = {
    val q = YFRealtimeStockQuoteService.getQuoteSync(symbol)
    if(q.error.isDefined) None
    else Some(JS(
      "symbol" -> q.symbol,
      "name" -> q.name,
      "exchange" -> q.exchange,
      "lastTrade" -> q.lastTrade,
      "time" -> q.time,
      "tradeDateTime" -> q.tradeDateTime,
      "change" -> q.change,
      "changePct" -> q.changePct,
      "prevClose" -> q.prevClose,
      "open" -> q.open,
      "close" -> q.close,
      "ask" -> q.ask,
      "askSize" -> q.askSize,
      "bid" -> q.bid,
      "bidSize" -> q.bidSize,
      "target1Yr" -> q.target1Yr,
      "beta" -> q.beta,
      "nextEarningsDate" -> q.nextEarningsDate,
      "low" -> q.low,
      "high" -> q.high,
      "spread" -> q.spread,
      "low52Week" -> q.low52Week,
      "high52Week" -> q.high52Week,
      "volume" -> q.volume,
      "avgVol3m" -> q.avgVol3m,
      "marketCap" -> q.marketCap,
      "peRatio" -> q.peRatio,
      "eps" -> q.eps,
      "dividend" -> q.dividend,
      "divYield" -> q.divYield,
      "responseTimeMsec" -> q.responseTimeMsec,
      "active" -> true))
  }

}
