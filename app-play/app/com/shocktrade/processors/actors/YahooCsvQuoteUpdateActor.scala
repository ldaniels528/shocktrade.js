package com.shocktrade.processors.actors

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.dao.SecuritiesUpdateDAO
import com.shocktrade.processors.TradingClock
import com.shocktrade.processors.actors.YahooCsvQuoteUpdateActor._
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument => BS}

/**
  * Yahoo! Finance CSV Quote Update Actor
  * @author lawrence.daniels@gmail.com
  */
class YahooCsvQuoteUpdateActor(reactiveMongoApi: ReactiveMongoApi) extends Actor with ActorLogging {
  private implicit val ec = context.dispatcher
  private val updateDAO = SecuritiesUpdateDAO(reactiveMongoApi)
  private val counter = new AtomicInteger()

  override def receive = {
    case RefreshAllQuotes =>
      if (TradingClock.isTradingActive) {
        log.info("Loading symbols for CSV updates...")
        val mySender = sender()

        counter.set(0)
        var count = 0
        updateDAO.getSymbolsForCsvUpdate.collect[Seq]() foreach { docs =>
          docs.flatMap(_.getAs[String]("symbol")).sliding(32, 32) foreach { symbols =>
            count += symbols.length
            self ! RefreshQuotes(symbols)
          }
          mySender ! count
        }
      }

    case RefreshQuotes(symbols) =>
      YFStockQuoteService.getQuotesSync(symbols, Parameters) foreach { q =>
        updateDAO.updateQuote(q.symbol, BS(
          "name" -> q.name,
          "exchange" -> q.exchange,
          "lastTrade" -> q.lastTrade,
          "tradeDate" -> q.tradeDate.map(t => BSONDateTime(t.getTime)).orNull,
          "tradeDateTime" -> q.tradeDateTime.map(t => BSONDateTime(t.getTime)).orNull,
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

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

}

/**
  * Yahoo! Finance CSV Quote Update Actor Singleton
  * @author lawrence.daniels@gmail.com
  */
object YahooCsvQuoteUpdateActor {
  private val Parameters = YFStockQuoteService.getParams(
    "symbol", "exchange", "name", "lastTrade", "tradeDate", "tradeTime", "change", "changePct", "prevClose", "open", "close", "high", "low",
    "high52Week", "low52Week", "volume", "marketCap", "errorMessage", "ask", "askSize", "bid", "bidSize")

  case object RefreshAllQuotes

  case class RefreshQuotes(symbols: Seq[String])

}
