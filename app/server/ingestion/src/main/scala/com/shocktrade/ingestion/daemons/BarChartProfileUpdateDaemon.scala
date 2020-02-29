package com.shocktrade.ingestion.daemons

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.bulk.BulkUpdateStatistics
import com.shocktrade.server.concurrent.{ConcurrentProcessor, Daemon}
import com.shocktrade.server.services.BarChartProfileService
import io.scalajs.npm.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}

/**
  * Bar Chart Profile Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BarChartProfileUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[BulkUpdateStatistics] {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  // get the DAO and service
  //private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
  private val profileService = new BarChartProfileService()

  // internal variables
  private val processor = new ConcurrentProcessor()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(clock: TradingClock): Boolean = !clock.isTradingActive

  /**
    * Executes the process
    * @param clock the given [[TradingClock trading clock]]
    */
  override def run(clock: TradingClock): Future[BulkUpdateStatistics] = {
    /*
    val startTime = js.Date.now()
    val outcome = for {
      securities <- securitiesDAO.flatMap(_.findSymbolsIfEmpty("description"))
      status <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef): Future[BulkUpdateOutcome] = {
          for {
            response_? <- profileService(security.symbol)
            w <- response_? match {
              case Some(response) => securitiesDAO.flatMap(_.updateProfile(response))
              case None => Future.failed(die(s"No Bar Chart profile response for symbol ${security.symbol}"))
            }
          } yield w.toBulkWrite
        }
      })
    } yield status

    outcome onComplete {
      case Success(stats) =>
        logger.info(s"$stats in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome*/???
  }

}
