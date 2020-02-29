package com.shocktrade.ingestion.daemons

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.ConcurrentProcessor
import com.shocktrade.server.services.yahoo.YahooFinanceKeyStatisticsService
import com.shocktrade.server.services.yahoo.YahooFinanceKeyStatisticsService.YFQuantityType

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.scalajs.js

/**
  * Key Statistics Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class KeyStatisticsUpdateDaemon()(implicit ec: ExecutionContext) {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  // create the DAO and services instances
  //private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
  private val yfKeyStatsSvc = new YahooFinanceKeyStatisticsService()

  // internal variables
  private val processor = new ConcurrentProcessor()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  def isReady(clock: TradingClock): Boolean = !clock.isTradingActive

  /**
    * Executes the process
    * @param clock the given [[TradingClock trading clock]]
    */
   def run(clock: TradingClock): Unit = {
     /*
    val startTime = js.Date.now()
    val outcome = for {
      securities <- securitiesDAO.flatMap(_.findSymbolsForKeyStatisticsUpdate(clock.getTradeStopTime))
      stats <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[SecurityRef](securities.size, reportInterval = 15.seconds) {
        logger.info(s"Scheduling ${securities.size} securities for processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef): Future[BulkUpdateOutcome] = {
          for {
            stats_? <- yfKeyStatsSvc(security.symbol)
            w <- stats_? match {
              case Some(stats) => securitiesDAO.flatMap(_.updateKeyStatistics(stats.toData(security)))
              case None => Future.failed(die(s"No key statistics response for symbol ${security.symbol}"))
            }
          } yield w.toBulkWrite
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
    outcome*/ ???
   }

}

/**
  * Key Statistics Update Process Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object KeyStatisticsUpdateDaemon {

  trait SecurityRef extends js.Object

  /**
    * Implicitly converts a quantity into a double value
    * @param quantity the given [[YFQuantityType quantity]]
    * @return the double value
    */
  implicit def quantityToDouble(quantity: js.UndefOr[YFQuantityType]): js.UndefOr[Double] = quantity.flatMap(_.raw)


}
