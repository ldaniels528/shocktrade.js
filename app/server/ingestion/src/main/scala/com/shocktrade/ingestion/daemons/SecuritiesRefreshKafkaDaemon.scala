package com.shocktrade.ingestion.daemons

import com.shocktrade.common.util.ExchangeHelper
import com.shocktrade.ingestion.daemons.SecuritiesRefreshKafkaDaemon._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.bulk.{BulkUpdateHandler, BulkUpdateOutcome, BulkUpdateStatistics}
import com.shocktrade.server.concurrent.{ConcurrentContext, ConcurrentProcessor, Daemon}
import com.shocktrade.server.dao.securities.{SecuritiesUpdateDAO, SecurityRef, SecurityUpdateQuote, SnapshotQuote}
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import io.scalajs.npm.kafkanode._
import io.scalajs.npm.mongodb.Db
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  * Securities Update Kafka Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SecuritiesRefreshKafkaDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, kafkaProducer: Producer) extends Daemon[BulkUpdateStatistics] {
  private implicit val logger = LoggerFactory.getLogger(getClass)
  private val topic = "com.shocktrade.yahoo.quotes.csv"
  private val batchSize = 40

  // get service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val cvsQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "open", "prevClose", "close", "high", "low", "change", "changePct",
    "high52Week", "low52Week", "tradeDate", "tradeTime", "volume", "marketCap", "target1Y", "errorMessage"
  )

  // get DAO references
  private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)

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
    val startTime = js.Date.now()
    val outcome = for {
      securities <- getSecurities(clock.getTradeStopTime)
      results <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[InputBatch](securities.size) {
        logger.info(s"Scheduling ${securities.size} pages of securities for updates and snapshots...")

        override def onNext(ctx: ConcurrentContext, securities: InputBatch): Future[BulkUpdateOutcome] = {
          val symbols = securities.map(_.symbol)
          for {
            quotes <- getYahooCSVQuotes(symbols)
            w <- persistSecurities(quotes).map(n => BulkUpdateOutcome(nInserted = n))
          } yield w
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
    outcome
  }

  private def getSecurities(cutOffTime: js.Date) = {
    for {
      securities <- securitiesDAO.flatMap(_.findSymbolsForFinanceUpdate(cutOffTime))
      batches = js.Array(securities.sliding(batchSize, batchSize).map(_.toSeq).toSeq: _*)
    } yield batches
  }

  private def getYahooCSVQuotes(symbols: Seq[String], attemptsLeft: Int = 2) = {
    csvQuoteSvc.getQuotes(cvsQuoteParams, symbols: _*) recover { case e =>
      logger.error(s"Service call failure [${e.getMessage}] for symbols: %s", symbols.mkString("+"))
      Seq.empty
    }
  }

  private def persistSecurities(quotes: Seq[YFCSVQuote]) = {
    val payloads = js.Array(quotes.zipWithIndex map { case (q, n) =>
      new ProduceRequest(topic, messages = JSON.stringify(q), partition = n % 10)
    }: _*)
    kafkaProducer.sendFuture(payloads) map (_ => quotes.length)
  }

}

/**
  * Securities Update Kafka Daemon Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesRefreshKafkaDaemon {

  type InputBatch = Seq[SecurityRef]

  /**
    * Yahoo! Finance CSV Quote Extensions
    * @param quote the given [[YFCSVQuote quote]]
    */
  implicit class YFCSVQuoteExtensions(val quote: YFCSVQuote) extends AnyVal {

    @inline
    def toUpdateQuote(mapping: js.Dictionary[SecurityRef]) = new SecurityUpdateQuote(
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
    )

    @inline
    def spread: js.UndefOr[Double] = {
      for {
        high <- quote.high
        low <- quote.low
      } yield if (high > 0) 100.0 * ((high - low) / high.toDouble) else 0.00
    }

    @inline
    def toSnapshot(mapping: js.Dictionary[SecurityRef]) = new SnapshotQuote(
      symbol = quote.symbol,
      exchange = quote.normalizedExchange(mapping),
      subExchange = quote.exchange,
      lastTrade = quote.lastTrade,
      tradeDateTime = quote.tradeDateTime,
      tradeDate = quote.tradeDate,
      tradeTime = quote.tradeTime,
      volume = quote.volume
    )

    @inline
    def normalizedExchange(mapping: js.Dictionary[SecurityRef]): String = {
      val originalExchange_? = mapping.get(quote.symbol).flatMap(_.exchange.toOption)
      originalExchange_? ?? quote.exchange.toOption.flatMap(ExchangeHelper.lookupExchange) match {
        case Some(exchange) => exchange
        case None if quote.symbol.endsWith(".OB") => "OTCBB"
        case None => originalExchange_? getOrElse "UNKNOWN"
      }
    }

  }

}