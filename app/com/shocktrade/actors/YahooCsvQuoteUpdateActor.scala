package com.shocktrade.actors

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.YahooCsvQuoteUpdateActor._
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import com.shocktrade.util.BSONHelper._
import reactivemongo.bson.{BSONDocument => BS}

/**
 * Yahoo! Finance CSV Quote Update Actor
 * @author lawrence.daniels@gmail.com
 */
class YahooCsvQuoteUpdateActor() extends Actor with ActorLogging {
  val counter = new AtomicInteger()

  override def receive = {
    case RefreshQuotes(symbols) =>
      YFStockQuoteService.getQuotesSync(symbols, Parameters) foreach { q =>
        StockQuotes.updateQuote(q.symbol, BS(
          "exchange" -> q.exchange,
          "lastTrade" -> q.lastTrade,
          "tradeDate" -> q.tradeDate,
          "tradeTime" -> q.tradeTime,
          "change" -> q.change,
          "changePct" -> q.changePct,
          "prevClose" -> q.prevClose,
          "open" -> q.open,
          "close" -> q.close,
          "high" -> q.high,
          "low" -> q.low,
          "high52Week" -> q.high52Week,
          "low52Week" -> q.low52Week,
          "volume" -> q.volume,
          "marketCap" -> q.marketCap,
          "ask" -> q.ask, "askSize" -> q.askSize,
          "bid" -> q.bid, "bidSize" -> q.bidSize,
          "yfDynLastUpdated" -> new Date(),
          "errorMessage" -> q.errorMessage
        ))

        // log the statistics
        if (counter.incrementAndGet() % 1000 == 0) {
          log.info(s"Processed ${counter.get} quotes")
        }
      }
    case message => unhandled(message)
  }
}

/**
 * Yahoo! Finance CSV Quote Update Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object YahooCsvQuoteUpdateActor {

  private val Parameters = YFStockQuoteService.getParams(
    "symbol", "exchange", "lastTrade", "tradeDate", "tradeTime", "change", "changePct", "prevClose", "open", "close", "high", "low",
    "high52Week", "low52Week", "volume", "marketCap", "errorMessage", "ask", "askSize", "bid", "bidSize")

  case class RefreshQuotes(symbols: Seq[String])

}
