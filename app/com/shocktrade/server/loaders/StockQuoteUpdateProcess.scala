package com.shocktrade.server.loaders

import akka.actor.{Actor, ActorLogging, Props}
import com.ldaniels528.tabular.Tabular
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.services.yahoofinance.YFStockQuoteService
import org.slf4j.LoggerFactory
import play.api.libs.json.Json.{obj => JS}
import play.libs.Akka

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * Stock Quote Update Process
 * @author lawrence.daniels@gmail.com
 */
object StockQuoteUpdateProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  private val system = Akka.system
  private val tabular = new Tabular()
  implicit val ec = system.dispatcher

  private val yfCsvActor = system.actorOf(Props[YahooCsvQuoteUpdateActor], name = "YfCsvActor")

  /**
   * Starts the process
   */
  def start() {
    logger.info("Starting Stock Quote Update Process ...")
    system.scheduler.schedule(5.seconds, 15.minutes) {
      logger.info("Loading symbols for CSV update...")
      StockQuotes.getSymbolsForCsvUpdate foreach { docs =>
        docs.flatMap(js => (js \ "symbol").asOpt[String]).sliding(32, 32) foreach { symbols =>
          yfCsvActor ! RefreshQuotes(symbols)
        }
      }
    }
    ()
  }

  class YahooCsvQuoteUpdateActor() extends Actor with ActorLogging {
    override def receive = {
      case RefreshQuotes(symbols) =>
        log.info(s"Loading: ${symbols.mkString(",")}")
        val quotes = YFStockQuoteService.getQuotesSync(symbols, params)
        tabular.transform(quotes) foreach log.info

      case message => unhandled(message)
    }
  }

  private val params = YFStockQuoteService.getParams(
    "symbol", "exchange", "lastTrade", "tradeDate", "tradeTime", "change", "changePct", "prevClose", "open", "close", "high", "low",
    "high52Week", "low52Week", "volume", "marketCap", "errorMessage", "ask", "askSize", "bid", "bidSize")

  logger.info(s"params = $params")

  case class RefreshQuotes(symbols: Seq[String])

}
