package com.shocktrade.ingestion.daemons

import com.shocktrade.ingestion.daemons.KeyStatisticsUpdateDaemon.SecurityRef
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.bulk.BulkUpdateStatistics
import com.shocktrade.server.concurrent.{ConcurrentProcessor, Daemon}
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import io.scalajs.nodejs._
import io.scalajs.npm.mongodb.Db

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js

/**
  * Securities Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SecuritiesUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[BulkUpdateStatistics] {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)
  private val batchSize = 40

  // get service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val csvQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "prevClose", "close", "high", "low", "change", "changePct",
    "high52Week", "low52Week", "tradeDate", "tradeTime", "volume", "marketCap", "target1Y", "errorMessage"
  )

  // get DAO references
  //private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
  //private val snapshotDAO = dbFuture.map(_.getSnapshotDAO)

  // internal variables
  private val processor = new ConcurrentProcessor()
  private var lastRun: js.Date = new js.Date()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(clock: TradingClock): Boolean = clock.isTradingActive || clock.isTradingActive(lastRun)

  /**
    * Executes the process
    * @param clock the given [[TradingClock trading clock]]
    */
  override def run(clock: TradingClock): Future[BulkUpdateStatistics] = {
    /*
    val startTime = js.Date.now()
    val outcome = for {
      securities <- getSecurities(clock.getTradeStopTime)
      results <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[InputBatch](securities.size) {
        logger.info(s"Scheduling ${securities.size} pages of securities for updates and snapshots...")

        override def onNext(ctx: ConcurrentContext, securities: InputBatch): Future[BulkUpdateOutcome] = {
          val symbols = securities.map(_.symbol)
          val mapping = js.Dictionary(securities.map(s => s.symbol -> s): _*)
          for {
            quotes <- getYahooCSVQuotes(symbols)
            w1 <- createSnapshots(snapshots = quotes.map(_.toSnapshot(mapping)), mapping)
            w2 <- updateSecurities(securities = quotes.map(_.toUpdateQuote(mapping)), mapping)
          } yield w1 + w2
        }
      })
    } yield results

    outcome onComplete {
      case Success(stats) =>
        lastRun = new js.Date(startTime)
        logger.info(s"$stats in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome*/ ???
  }

  private def getSecurities(cutOffTime: js.Date): Future[js.Array[Seq[SecurityRef]]] = {
    /*
    for {
      securities <- securitiesDAO.flatMap(_.findSymbolsForFinanceUpdate(cutOffTime))
      batches = js.Array(securities.sliding(batchSize, batchSize).map(_.toSeq).toSeq: _*)
    } yield batches*/ ???
  }
/*
  private def getYahooCSVQuotes(symbols: Seq[String], attemptsLeft: Int = 2): Future[Seq[YFCSVQuote]] = {
    csvQuoteSvc.getQuotes(csvQuoteParams, symbols: _*) recover { case e =>
      logger.error(s"Service call failure [${e.getMessage}] for symbols: %s", symbols.mkString("+"))
      Seq.empty
    }
  }

  private def createSnapshots(snapshots: Seq[SnapshotQuote], mapping: js.Dictionary[SecurityRef]): Future[BulkUpdateOutcome] = {
    def insertSnapshot(): Future[BulkUpdateOutcome] = snapshotDAO.flatMap(_.updateSnapshots(snapshots).toFuture.map(_.toBulkWrite))

    def retrySnapshot(duration: FiniteDuration): Future[BulkUpdateOutcome] = retry(() => insertSnapshot(), duration)

    insertSnapshot() /*fallbackTo retrySnapshot(5.seconds)*/ recover { case e =>
      logger.error("Snapshot insert error: %s", e.getMessage)
      BulkUpdateOutcome(nFailures = snapshots.size)
    }
  }

  private def updateSecurities(securities: Seq[SecurityUpdateQuote], mapping: js.Dictionary[SecurityRef]): Future[BulkUpdateOutcome] = {
    def upsertSecurities(): Future[BulkUpdateOutcome] = securitiesDAO.flatMap(_.updateSecurities(securities).toFuture.map(_.toBulkWrite))

    def retrySecurities(duration: FiniteDuration): Future[BulkUpdateOutcome] = retry(() => upsertSecurities(), duration)

    upsertSecurities() /*fallbackTo retrySecurities(5.seconds)*/ recover { case e =>
      logger.error("Securities update error: %s", e.getMessage)
      BulkUpdateOutcome(nFailures = securities.size)
    }
  }*/

  private def retry[T](f: () => Future[T], duration: FiniteDuration): Future[T] = {
    val promise = Promise[T]()
    setTimeout(() => {
      f() onComplete promise.complete
    }, duration)
    promise.future
  }

}

/**
  * Securities Update Daemon Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesUpdateDaemon {

  type InputBatch = Seq[SecurityRef]

  trait SnapshotQuote extends js.Object

  /**
    * Yahoo! Finance CSV Quote Extensions
    * @param quote the given [[YFCSVQuote quote]]
    */
  implicit class YFCSVQuoteExtensions(val quote: YFCSVQuote) extends AnyVal {

    @inline
    def toUpdateQuote(mapping: js.Dictionary[SecurityRef]) = ??? /*new SecurityUpdateQuote(
      symbol = quote.symbol,
      exchange = quote.normalizedExchange(mapping),
      subExchange = quote.exchange,
      lastTrade = quote.lastTrade,
      prevClose = quote.prevClose,
      open = quote.open,
      close = quote.close,
      high = quote.high,
      low = quote.low,
      spread = quote.spread,
      change = quote.change,
      changePct = quote.changePct,
      high52Week = quote.high52Week,
      low52Week = quote.low52Week,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume,
      marketCap = quote.marketCap,
      target1Yr = quote.target1Yr,
      active = true,
      errorMessage = quote.errorMessage,
      yfCsvResponseTime = quote.responseTimeMsec,
      yfCsvLastUpdated = new js.Date()
    )*/

    @inline
    def spread: js.UndefOr[Double] = {
      for {
        high <- quote.high
        low <- quote.low
      } yield if (high > 0) 100.0 * ((high - low) / high.toDouble) else 0.00
    }

    @inline
    def toSnapshot(mapping: js.Dictionary[SecurityRef]) = ??? /* new SnapshotQuote(
      symbol = quote.symbol,
      exchange = quote.normalizedExchange(mapping),
      subExchange = quote.exchange,
      lastTrade = quote.lastTrade,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume
    )*/

    @inline
    def normalizedExchange(mapping: js.Dictionary[SecurityRef]): String = ??? /*{
      val originalExchange_? = mapping.get(quote.symbol).flatMap(_.exchange.toOption)
      originalExchange_? ?? quote.exchange.toOption.flatMap(ExchangeHelper.lookupExchange) match {
        case Some(exchange) => exchange
        case None if quote.symbol.endsWith(".OB") => "OTCBB"
        case None => originalExchange_? getOrElse "UNKNOWN"
      }
    }*/

  }

}