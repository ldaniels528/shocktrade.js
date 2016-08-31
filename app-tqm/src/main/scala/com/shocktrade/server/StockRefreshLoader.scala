package com.shocktrade.server

import com.shocktrade.server.data.StockUpdateDAO._
import com.shocktrade.server.services.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.YahooFinanceCSVQuotesService.YFCSVQuote
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Stock Refresh Loader
  * @author lawrence.daniels@gmail.com
  */
class StockRefreshLoader(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
  private val batchSize = 40

  // get DAO and service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume", "errorMessage"
  )
  private val stockDAO = dbFuture.flatMap(_.getStockDAO)

  def run(): Unit = {
    val startTime = js.Date.now()
    val outcome = for {
      quoteRefs <- stockDAO.flatMap(_.findSymbolsForUpdate())
      quotesWithResults <- processQuotes(quoteRefs)
    } yield quotesWithResults

    outcome onComplete {
      case Success(quotesWithResults) =>
        console.log(s"Process completed in ${js.Date.now() - startTime} msec(s)")
      case Failure(e) =>
        console.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def processQuotes(quoteRefs: Seq[StockQuoteRef]) = {
    console.log(s"Retrieved ${quoteRefs.size} symbols (${quoteRefs.size / batchSize} batches expected)")
    Future.sequence {
      var batchNo = 0
      quoteRefs.sliding(batchSize, batchSize).toSeq map { batch =>
        val symbols = batch.map(_.symbol)
        batchNo += 1
        console.log(s"[$batchNo] Querying ${symbols.size} symbols (from '${symbols.head}' to '${symbols.last}')")

        for {
          quotes <- csvQuoteSvc.getQuotes(cvsQuoteParams, symbols)
          quotesWithResults <- updateQuotes(quotes)
        } yield quotesWithResults
      }
    }
  }

  @inline
  private def updateQuotes(quotes: Seq[YFCSVQuote]) = {
    stockDAO.flatMap(_.updateQuotes(quotes).toFuture) map (quotes -> _)
  }

}
