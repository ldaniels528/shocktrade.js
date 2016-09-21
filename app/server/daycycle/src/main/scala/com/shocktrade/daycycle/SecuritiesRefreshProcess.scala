package com.shocktrade.daycycle

import com.shocktrade.common.dao.quotes.SecuritiesSnapshotDAO._
import com.shocktrade.common.dao.quotes.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.quotes.{SecurityUpdateQuote, SnapshotQuote}
import com.shocktrade.daycycle.SecuritiesRefreshProcess._
import com.shocktrade.services.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.services.{TradingClock, YahooFinanceCSVQuotesService}
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.{BulkWriteOpResultObject, Db}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Securities Refresh Process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SecuritiesRefreshProcess(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
  private val batchSize = 40

  // get service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume", "errorMessage"
  )

  // load the modules
  private val moment = Moment()

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
          log(s"Process completed in %d seconds", (js.Date.now() - startTime) / 1000)
        case Failure(e) =>
          console.error(s"Failed during processing: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  private def processQuotes(quoteRefs: Seq[SecuritiesRef]) = {
    log(s"Retrieved ${quoteRefs.size} symbols (${quoteRefs.size / batchSize} batches expected)")
    Future.sequence {
      var batchNo = 0
      var symbolCount = 0
      quoteRefs.sliding(batchSize, batchSize).toSeq map { batch =>
        val symbols = batch.flatMap(_.symbol.toOption)
        batchNo += 1
        symbolCount += symbols.size

        // notify the operator at every 100 batches
        if (batchNo % 100 == 0 || symbolCount == quoteRefs.size) {
          val completion = (100 * (symbolCount.toDouble / quoteRefs.size)).toInt
          log("Processed %d securities (%d%% complete - %d batches) so far...", symbolCount, completion, batchNo)
        }

        for {
          quotes <- csvQuoteSvc.getQuotes(cvsQuoteParams, symbols)
          snapshotResults <- createSnapshots(quotes) recover { case e =>
            console.error("Snapshot write error: %s", e.getMessage)
            New[BulkWriteOpResultObject]
          }
          securitiesResults <- updateSecurities(quotes) recover { case e =>
            console.error("Securities update error: %s", e.getMessage)
            New[BulkWriteOpResultObject]
          }
        } yield snapshotResults -> securitiesResults
      }
    }
  }

  @inline
  private def log(message: String, args: js.Any*) = {
    console.log(s"[${moment().format("MM/DD HH:mm:ss")}] " + message, args: _*)
  }

  @inline
  private def createSnapshots(quotes: Seq[YFCSVQuote]) = {
    snapshotDAO.flatMap(_.updateSnapshots(quotes.map(_.toSnapshot)).toFuture)
  }

  @inline
  private def updateSecurities(quotes: Seq[YFCSVQuote]) = {
    securitiesDAO.flatMap(_.updateQuotes(quotes.map(_.toUpdateQuote)).toFuture)
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
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      tradeDateTime = quote.tradeDateTime,
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