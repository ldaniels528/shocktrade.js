package com.shocktrade.daycycle

import com.shocktrade.common.dao.quotes.SecuritiesSnapshotDAO._
import com.shocktrade.common.dao.quotes.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.quotes.{SecurityUpdateQuote, SnapshotQuote}
import com.shocktrade.daycycle.SecuritiesRefreshProcess._
import com.shocktrade.services.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.services.{TradingClock, YahooFinanceCSVQuotesService}
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.moment.timezone.MomentTimezone
import org.scalajs.nodejs.mongodb.Db
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

  // get DAO and service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume", "errorMessage"
  )
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val snapshotDAO = dbFuture.flatMap(_.getSnapshotDAO)

  // load the modules
  implicit val moment = Moment()
  implicit val momentTz = MomentTimezone()

  // create a trading clock
  private val tradingClock = new TradingClock()

  def run(): Unit = {
    // if trading is active, run the process ...
    if (tradingClock.isTradingActive) {
      val startTime = js.Date.now()
      val outcome = for {
        quoteRefs <- securitiesDAO.flatMap(_.findSymbolsForUpdate())
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
  }

  private def processQuotes(quoteRefs: Seq[SecuritiesRef]) = {
    console.log(s"Retrieved ${quoteRefs.size} symbols (${quoteRefs.size / batchSize} batches expected)")
    Future.sequence {
      var batchNo = 0
      quoteRefs.sliding(batchSize, batchSize).toSeq map { batch =>
        val symbols = batch.flatMap(_.symbol.toOption)
        batchNo += 1
        console.log(s"[$batchNo] Querying ${symbols.size} symbols (from '${symbols.head}' to '${symbols.last}')")

        for {
          quotes <- csvQuoteSvc.getQuotes(cvsQuoteParams, symbols)
          snapshotResults <- createSnapshots(quotes)
          securitiesResults <- updateQuotes(quotes)
        } yield securitiesResults
      }
    }
  }

  @inline
  private def createSnapshots(quotes: Seq[YFCSVQuote]) = {
    snapshotDAO.flatMap(_.updateSnapshots(quotes.map(_.toSnapshot)).toFuture)
  }

  @inline
  private def updateQuotes(quotes: Seq[YFCSVQuote]) = {
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
      tradeTime = quote.tradeTime,
      volume = quote.volume
    )
  }

}