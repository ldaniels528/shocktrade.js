package com.shocktrade.server.loaders

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import com.ldaniels528.tabular.Tabular
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.DateUtil
import play.api.Logger
import play.libs.Akka
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
 * Stock Quote Update Process
 * @author lawrence.daniels@gmail.com
 */
object StockQuoteUpdateProcess {
  private val system = Akka.system
  private val tabular = new Tabular()
  implicit val ec = system.dispatcher

  private val yfCsvActor = system.actorOf(Props[YahooCsvQuoteUpdateActor], name = "YfCsvActor")

  /**
   * Starts the process
   */
  def start() {
    Logger.info("Starting Stock Quote Update Process ...")
    system.scheduler.schedule(5.seconds, 1.hour) {
      if (isTradingActive) {
        Logger.info("Loading symbols for CSV update...")
        StockQuotes.getSymbolsForCsvUpdate onComplete {
          case Success(docs) =>
            if (docs.isEmpty) Logger.info("StockQuoteUpdateProcess: No data returned...")
            else {
              docs.flatMap(_.getAs[String]("symbol")).sliding(32, 32) foreach { symbols =>
                yfCsvActor ! RefreshQuotes(symbols)
              }
            }
          case Failure(e) =>
            Logger.error("Failed to retrieve CSV symbols for update", e)
        }
      }
    }
    ()
  }

  private def isTradingActive = DateUtil.isTradingActive

  /**
   * Yahoo! Finance CSV Quote Update Actor
   * @author lawrence.daniels@gmail.com
   */
  class YahooCsvQuoteUpdateActor() extends Actor with ActorLogging {
    val counter = new AtomicInteger()

    override def receive = {
      case RefreshQuotes(symbols) =>
        YFStockQuoteService.getQuotesSync(symbols, params) foreach { q =>
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
          if (counter.incrementAndGet() % 100 == 0) {
            log.info(s"Processed ${counter.get} quotes")
          }
        }
      case message => unhandled(message)
    }
  }

  private val params = YFStockQuoteService.getParams(
    "symbol", "exchange", "lastTrade", "tradeDate", "tradeTime", "change", "changePct", "prevClose", "open", "close", "high", "low",
    "high52Week", "low52Week", "volume", "marketCap", "errorMessage", "ask", "askSize", "bid", "bidSize")

  Logger.info(s"params = $params")

  case class RefreshQuotes(symbols: Seq[String])

}
