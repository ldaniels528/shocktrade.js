package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.KeyStatisticsDAO._
import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.{KeyStatisticsData, SecurityRef}
import com.shocktrade.concurrent.ConcurrentProcessor
import com.shocktrade.concurrent.daemon.{BulkTaskUpdateHandler, Daemon}
import com.shocktrade.daycycle.daemons.KeyStatisticsUpdateDaemon._
import com.shocktrade.services.YahooFinanceKeyStatisticsService.{YFKeyStatistics, YFQuantityType}
import com.shocktrade.services._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Key Statistics Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class KeyStatisticsUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private val logger = LoggerFactory.getLogger(getClass)

  // create the DAO and services instances
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val keyStatisticsDAO = dbFuture.flatMap(_.getKeyStatisticsDAO)
  private val yfKeyStatsSvc = new YahooFinanceKeyStatisticsService()

  // internal variables
  private val processor = new ConcurrentProcessor()
  private val tradingClock = new TradingClock()

  /**
    * Executes the process
    */
  def run(): Unit = {
    val startTime = js.Date.now()
    val outcome = for {
      securities <- lookupSecurities(tradingClock.getTradeStopTime)
      stats <- processor.start(securities, concurrency = 15, handler = new BulkTaskUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for Key Statistics processing...")

        override def processBatch(security: SecurityRef) = {
          for {
            stats_? <- yfKeyStatsSvc(security.symbol)
            result <- stats_? match {
              case Some(stats) => keyStatisticsDAO.flatMap(_.saveKeyStatistics(stats.toData(security)))
              case None => Future.failed(die(s"No key statistics response for symbol ${security.symbol}"))
            }
          } yield (1, result)
        }
      })
    } yield stats

    outcome onComplete {
      case Success(stats) =>
        logger.info(s"$stats in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  @inline
  def lookupSecurities(cutOffTime: js.Date) = {
    securitiesDAO.flatMap(_.findSymbolsForKeyStatisticsUpdate(cutOffTime))
  }

}

/**
  * Key Statistics Update Process Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object KeyStatisticsUpdateDaemon {

  /**
    * Implicitly converts a quantity into a double value
    * @param quantity the given [[YFQuantityType quantity]]
    * @return the double value
    */
  implicit def quantityToDouble(quantity: js.UndefOr[YFQuantityType]): js.UndefOr[Double] = quantity.flatMap(_.raw)

  /**
    * Yahoo! Finance: Key Statistics Extensions
    * @param stats the given [[YFKeyStatistics statistics]]
    */
  implicit class YFKeyStatisticsExtensions(val stats: YFKeyStatistics) extends AnyVal {

    @inline
    def toData(security: SecurityRef) = {
      new KeyStatisticsData(
        _id = security._id,
        symbol = security.symbol,
        exchange = stats.price.flatMap(_.exchange),
        ask = stats.summaryDetail.flatMap(_.ask),
        askSize = stats.summaryDetail.flatMap(_.askSize),
        averageDailyVolume10Day = stats.summaryDetail.flatMap(_.averageDailyVolume10Day),
        averageVolume = stats.summaryDetail.flatMap(_.averageVolume),
        averageVolume10days = stats.summaryDetail.flatMap(_.averageVolume10days),
        beta = stats.summaryDetail.flatMap(_.beta),
        bid = stats.summaryDetail.flatMap(_.bid),
        bidSize = stats.summaryDetail.flatMap(_.bidSize),
        dayHigh = stats.summaryDetail.flatMap(_.dayHigh),
        dayLow = stats.summaryDetail.flatMap(_.dayLow),
        dividendRate = stats.summaryDetail.flatMap(_.dividendRate),
        dividendYield = stats.summaryDetail.flatMap(_.dividendYield),
        exDividendDate = stats.summaryDetail.flatMap(_.exDividendDate),
        expireDate = stats.summaryDetail.flatMap(_.expireDate),
        fiftyDayAverage = stats.summaryDetail.flatMap(_.fiftyDayAverage),
        fiftyTwoWeekHigh = stats.summaryDetail.flatMap(_.fiftyTwoWeekHigh),
        fiftyTwoWeekLow = stats.summaryDetail.flatMap(_.fiftyTwoWeekLow),
        fiveYearAvgDividendYield = stats.summaryDetail.flatMap(_.fiveYearAvgDividendYield),
        forwardPE = stats.summaryDetail.flatMap(_.forwardPE),
        marketCap = stats.summaryDetail.flatMap(_.marketCap),
        maxAge = stats.summaryDetail.flatMap(_.maxAge),
        navPrice = stats.summaryDetail.flatMap(_.navPrice),
        openInterest = stats.summaryDetail.flatMap(_.openInterest),
        postMarketChange = stats.price.flatMap(_.postMarketChange),
        postMarketChangePercent = stats.price.flatMap(_.postMarketChangePercent),
        postMarketPrice = stats.price.flatMap(_.postMarketPrice),
        postMarketSource = stats.price.flatMap(_.postMarketSource),
        postMarketTime = stats.price.flatMap(_.postMarketTime),
        preMarketChange = stats.price.flatMap(_.preMarketChange),
        preMarketPrice = stats.price.flatMap(_.preMarketPrice),
        preMarketSource = stats.price.flatMap(_.preMarketSource),
        previousClose = stats.summaryDetail.flatMap(_.previousClose),
        priceToSalesTrailing12Months = stats.summaryDetail.flatMap(_.priceToSalesTrailing12Months),
        regularMarketDayLow = stats.summaryDetail.flatMap(_.regularMarketDayLow),
        regularMarketOpen = stats.summaryDetail.flatMap(_.regularMarketOpen),
        regularMarketPreviousClose = stats.summaryDetail.flatMap(_.regularMarketPreviousClose),
        regularMarketVolume = stats.summaryDetail.flatMap(_.regularMarketVolume),
        strikePrice = stats.summaryDetail.flatMap(_.strikePrice),
        totalAssets = stats.summaryDetail.flatMap(_.totalAssets),
        trailingAnnualDividendRate = stats.summaryDetail.flatMap(_.trailingAnnualDividendRate),
        trailingAnnualDividendYield = stats.summaryDetail.flatMap(_.trailingAnnualDividendYield),
        trailingPE = stats.summaryDetail.flatMap(_.trailingPE),
        twoHundredDayAverage = stats.summaryDetail.flatMap(_.twoHundredDayAverage),
        volume = stats.summaryDetail.flatMap(_.volume),
        `yield` = stats.summaryDetail.flatMap(_.`yield`),
        ytdReturn = stats.summaryDetail.flatMap(_.ytdReturn),
        lastUpdated = new js.Date()
      )
    }

  }

}
