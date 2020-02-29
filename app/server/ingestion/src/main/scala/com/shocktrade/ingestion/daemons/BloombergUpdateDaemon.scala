package com.shocktrade.ingestion.daemons

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.ConcurrentProcessor
import com.shocktrade.server.services.BloombergQuoteService

import scala.concurrent.ExecutionContext

/**
  * Bloomberg Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BloombergUpdateDaemon()(implicit ec: ExecutionContext) {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  // DAO & services
  private val quoteService = new BloombergQuoteService()

  // internal variables
  //private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
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
      securities <- securitiesDAO.flatMap(_.findSymbolsIfEmpty("sector"))
      status <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef): Future[BulkUpdateOutcome] = {
          for {
            response_? <- quoteService(security.symbol)
            result <- response_? match {
              case Some(response) => securitiesDAO.flatMap(_.updateBloomberg(security.symbol, response))
              case None => Future.failed(die(s"No Bloomberg response for symbol ${security.symbol}"))
            }
          } yield result.toBulkWrite
        }
      })
    } yield status

    outcome onComplete {
      case Success(status) =>
        logger.info(s"$status in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome*/???
  }

}
