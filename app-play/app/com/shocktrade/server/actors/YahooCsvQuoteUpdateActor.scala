package com.shocktrade.server.actors

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.actors.YahooCsvQuoteUpdateActor._
import com.shocktrade.server.trading.TradingClock
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import com.shocktrade.util.BSONHelper._
import play.libs.Akka
import reactivemongo.bson.{BSONDateTime, BSONDocument => BS}

import scala.concurrent.ExecutionContext

/**
 * Yahoo! Finance CSV Quote Update Actor
 * @author lawrence.daniels@gmail.com
 */
class YahooCsvQuoteUpdateActor() extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private val counter = new AtomicInteger()

  override def receive = {
    case RefreshAllQuotes =>
      if (TradingClock.isTradingActive) {
        log.info("Loading symbols for CSV updates...")
        val mySender = sender()

        counter.set(0)
        var count = 0
        StockQuotes.getSymbolsForCsvUpdate foreach { docs =>
          docs.flatMap(_.getAs[String]("symbol")).sliding(32, 32) foreach { symbols =>
            count += symbols.length
            self ! RefreshQuotes(symbols)
          }
          mySender ! count
        }
      }

    case RefreshQuotes(symbols) =>
      YFStockQuoteService.getQuotesSync(symbols, Parameters) foreach { q =>
        StockQuotes.updateQuote(q.symbol, BS(
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

  private val myActor = Akka.system.actorOf(Props[YahooCsvQuoteUpdateActor], name = "CsvQuote")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case object RefreshAllQuotes

  case class RefreshQuotes(symbols: Seq[String])

}
