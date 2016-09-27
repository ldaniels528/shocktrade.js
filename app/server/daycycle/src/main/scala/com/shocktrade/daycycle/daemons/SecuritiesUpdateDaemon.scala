package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesSnapshotDAO._
import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.{SecurityRef, SecurityUpdateQuote, SnapshotQuote}
import com.shocktrade.concurrent.ConcurrentProcessor
import com.shocktrade.concurrent.daemon.{BulkTaskUpdateHandler, Daemon}
import com.shocktrade.daycycle.daemons.SecuritiesUpdateDaemon._
import com.shocktrade.services.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.services.{LoggerFactory, TradingClock, YahooFinanceCSVQuotesService}
import com.shocktrade.util.ExchangeHelper
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.{BulkWriteOpResultObject, Db}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Securities Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SecuritiesUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private val logger = LoggerFactory.getLogger(getClass)
  private val batchSize = 40

  // get service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume", "errorMessage"
  )

  // get DAO references
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val snapshotDAO = dbFuture.flatMap(_.getSnapshotDAO)

  // create a trading clock
  private val tradingClock = new TradingClock()
  private var lastRun: js.Date = new js.Date()

  // internal variables
  private val processor = new ConcurrentProcessor()

  /**
    * Executes the process if trading is active
    */
  def run(): Unit = {
    // if trading is active, run the process ...
    if (tradingClock.isTradingActive || tradingClock.isTradingActive(lastRun)) {
      val startTime = js.Date.now()
      execute(startTime) onComplete {
        case Success(_) => lastRun = new js.Date(startTime)
        case Failure(e) => lastRun = new js.Date(startTime)
      }
    }
  }

  /**
    * Executes the process
    */
  def execute(startTime: Double) = {
    val outcome = for {
      securities <- getSecurities(tradingClock.getTradeStopTime)
      results <- processor.start(securities, concurrency = 20, handler = new BulkTaskUpdateHandler[InputBatch](securities.size) {
        logger.info(s"Scheduling ${securities.size} batches of securities for updates and snapshots...")

        override def processBatch(securities: InputBatch) = {
          val symbols = securities.map(_.symbol)
          for {
            quotes <- getYahooCSVQuotes(symbols)
            snapshotResults <- createSnapshots(quotes) recover { case e =>
              logger.error("Snapshot write error: %s", e.getMessage)
              New[BulkWriteOpResultObject]
            }
            securitiesResults <- updateSecurities(quotes)
          } yield (quotes.length, securitiesResults)
        }
      })
    } yield results

    outcome onComplete {
      case Success(status) =>
        logger.info(s"$status in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  private def getSecurities(cutOffTime: js.Date) = {
    for {
      securities <- securitiesDAO.flatMap(_.findSymbolsForUpdate(cutOffTime))
      batches = js.Array(securities.sliding(batchSize, batchSize).map(_.toSeq).toSeq: _*)
    } yield batches
  }

  private def getYahooCSVQuotes(symbols: Seq[String], attemptsLeft: Int = 2) = {
    csvQuoteSvc.getQuotes(cvsQuoteParams, symbols) recover { case e =>
      logger.error(s"Service call failure [${e.getMessage}] for symbols: %s", symbols.mkString("+"))
      Seq.empty
    }
  }

  private def createSnapshots(quotes: Seq[YFCSVQuote]) = {
    Try(quotes.map(_.toSnapshot)) match {
      case Success(snapshots) => snapshotDAO.flatMap(_.updateSnapshots(snapshots).toFuture)
      case Failure(e) =>
        Future.failed(die(s"Failed to convert at least one object to a snapshot: ${e.getMessage}"))
    }
  }

  private def updateSecurities(quotes: Seq[YFCSVQuote]) = {
    Try(quotes.map(_.toUpdateQuote)) match {
      case Success(securities) => securitiesDAO.flatMap(_.updateQuotes(securities).toFuture)
      case Failure(e) =>
        Future.failed(die(s"Failed to convert at least one object to a security: ${e.getMessage}"))
    }
  }

}

/**
  * Securities Refresh Process Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesUpdateDaemon {

  type InputBatch = Seq[SecurityRef]

  /**
    * Yahoo! Finance CSV Quote Extensions
    * @param quote the given [[YFCSVQuote quote]]
    */
  implicit class YFCSVQuoteExtensions(val quote: YFCSVQuote) extends AnyVal {

    @inline
    def toUpdateQuote = new SecurityUpdateQuote(
      symbol = quote.symbol,
      exchange = quote.normalizeExchange,
      subExchange = quote.exchange,
      lastTrade = quote.lastTrade,
      open = quote.open,
      close = quote.close,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume,
      errorMessage = quote.errorMessage,
      yfCsvResponseTime = quote.responseTimeMsec,
      yfCsvLastUpdated = new js.Date()
    )

    @inline
    def toSnapshot = new SnapshotQuote(
      symbol = quote.symbol,
      exchange = quote.normalizeExchange,
      subExchange = quote.exchange,
      lastTrade = quote.lastTrade,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume
    )

    @inline
    def normalizeExchange = {
      (for {
        subExchange <- quote.exchange.flat.toOption
        exchange <- ExchangeHelper.lookupExchange(subExchange)
      } yield exchange) getOrElse "UNKNOWN"
    }

  }

}