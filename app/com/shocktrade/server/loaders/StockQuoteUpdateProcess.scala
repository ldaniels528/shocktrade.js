package com.shocktrade.server.loaders

import akka.actor.Props
import com.shocktrade.actors.YahooCsvQuoteUpdateActor
import com.shocktrade.actors.YahooCsvQuoteUpdateActor.RefreshQuotes
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.util.DateUtil
import play.api.Logger
import play.libs.Akka

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
 * Stock Quote Update Process
 * @author lawrence.daniels@gmail.com
 */
object StockQuoteUpdateProcess {
  private val system = Akka.system
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

}
