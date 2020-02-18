package com.shocktrade.daycycle.daemons

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.bulk.BulkUpdateOutcome._
import com.shocktrade.server.concurrent.bulk.{BulkUpdateHandler, BulkUpdateOutcome, BulkUpdateStatistics}
import com.shocktrade.server.concurrent.{ConcurrentContext, ConcurrentProcessor, Daemon}
import com.shocktrade.server.dao.securities.{SecuritiesUpdateDAO, SecurityRef}
import com.shocktrade.server.services.CikLookupService
import io.scalajs.npm.mongodb.Db
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * CIK Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CikUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[BulkUpdateStatistics] {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  // get the DAO and service
  private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
  private val cikLookupService = new CikLookupService()

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
    val startTime = js.Date.now()
    val outcome = for {
      securities <- securitiesDAO.flatMap(_.findSymbolsIfEmpty("cikNumber"))
      status <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef): Future[BulkUpdateOutcome] = {
          for {
            response_? <- cikLookupService(security.symbol)
            result <- response_? match {
              case Some(response) => securitiesDAO.flatMap(_.updateCik(response))
              case None => Future.failed(die(s"No CIK response for symbol ${security.symbol}"))
            }
          } yield result.toBulkWrite
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
    outcome
  }

}
