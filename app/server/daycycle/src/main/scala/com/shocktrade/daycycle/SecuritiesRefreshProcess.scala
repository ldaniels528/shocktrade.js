package com.shocktrade.daycycle

import com.shocktrade.common.dao.quotes.SecuritiesSnapshotDAO._
import com.shocktrade.common.dao.quotes.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.quotes.{SecurityUpdateQuote, SnapshotQuote}
import com.shocktrade.daycycle.SecuritiesRefreshProcess._
import com.shocktrade.services.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.services.{LoggerFactory, TradingClock, YahooFinanceCSVQuotesService}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.{BulkWriteOpResultObject, Db}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Securities Refresh Process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SecuritiesRefreshProcess(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
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

  def run(): Unit = {
    // if trading is active, run the process ...
    if (tradingClock.isTradingActive) {
      val startTime = js.Date.now()
      val outcome = for {
        quoteRefs <- securitiesDAO.flatMap(_.findSymbolsForUpdate())
        results <- processQuotes(quoteRefs)
      } yield results

      outcome onComplete {
        case Success(results) =>
          logger.log(s"Process completed in %d seconds", (js.Date.now() - startTime) / 1000)
        case Failure(e) =>
          logger.error(s"Failed during processing: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  private def processQuotes(quoteRefs: Seq[SecuritiesRef]) = {
    logger.log(s"Retrieved ${quoteRefs.size} symbols (${quoteRefs.size / batchSize} batches expected)")
    Future.sequence {
      var scheduled = 0
      var scheduledBatchNo = 0
      var processed = 0
      var processedBatchNo = 0

      quoteRefs.sliding(batchSize, batchSize).toSeq map { batch =>
        val symbols = batch.flatMap(_.symbol.toOption)
        scheduledBatchNo += 1
        scheduled += symbols.size

        // notify the operator at every 100 batches
        if (scheduledBatchNo % 100 == 0 || scheduled == quoteRefs.size) {
          val completion = (100 * (scheduled.toDouble / quoteRefs.size)).toInt
          logger.log("Scheduled %d securities (%d%% completed - %d batches) so far...", scheduled, completion, scheduledBatchNo)
        }

        for {
          quotes <- getQuotes(symbols)
          snapshotResults <- createSnapshots(quotes) recover { case e =>
            logger.error("Snapshot write error: %s", e.getMessage)
            New[BulkWriteOpResultObject]
          }
          securitiesResults <- updateSecurities(quotes) recover { case e =>
            logger.error("Securities update error: %s", e.getMessage)
            New[BulkWriteOpResultObject]
          }
        } yield {
          // report the progress
          processed += quotes.length
          processedBatchNo += 1
          if (processedBatchNo % 100 == 0 || processed == quoteRefs.size) {
            val completion = (100 * (processed.toDouble / quoteRefs.size)).toInt
            logger.log("Persisted %d securities (%d%% completed - %d batches) so far...", processed, completion, processedBatchNo)
          }

          snapshotResults -> securitiesResults
        }
      }
    }
  }

  @inline
  private def getQuotes(symbols: Seq[String], attemptsLeft: Int = 2) = {
    csvQuoteSvc.getQuotes(cvsQuoteParams, symbols) recover { case e =>
      logger.error(s"Service call failure [${e.getMessage}] for symbols: %s", symbols.mkString("+"))
      Seq.empty
    }
  }

  @inline
  private def createSnapshots(quotes: Seq[YFCSVQuote]) = {
    Try(quotes.map(_.toSnapshot)) match {
      case Success(snapshots) => snapshotDAO.flatMap(_.updateSnapshots(snapshots).toFuture)
      case Failure(e) =>
        logger.error(s"Failed to convert at least one object to a snapshot: ${e.getMessage}")
        Future.successful(New[BulkWriteOpResultObject])
    }
  }

  @inline
  private def updateSecurities(quotes: Seq[YFCSVQuote]) = {
    Try(quotes.map(_.toUpdateQuote)) match {
      case Success(securities) => securitiesDAO.flatMap(_.updateQuotes(securities).toFuture)
      case Failure(e) =>
        logger.error(s"Failed to convert at least one object to a security: ${e.getMessage}")
        Future.successful(New[BulkWriteOpResultObject])
    }
  }

}

/**
  * Securities Refresh Process Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesRefreshProcess {

  /**
    * Yahoo! Finance CSV Quote Extensions
    * @param quote the given [[YFCSVQuote quote]]
    */
  implicit class YFCSVQuoteExtensions(val quote: YFCSVQuote) extends AnyVal {

    @inline
    def toUpdateQuote = new SecurityUpdateQuote(
      symbol = quote.symbol,
      exchange = quote.exchange,
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
      exchange = quote.exchange,
      lastTrade = quote.lastTrade,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume
    )
  }

}