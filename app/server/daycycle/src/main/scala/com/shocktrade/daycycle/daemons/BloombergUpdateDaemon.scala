package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.SecurityRef
import com.shocktrade.concurrent.bulk.BulkUpdateHandler
import com.shocktrade.concurrent.bulk.BulkUpdateOutcome._
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentProcessor, Daemon}
import com.shocktrade.serverside.{LoggerFactory, TradingClock}
import com.shocktrade.services.BloombergQuoteService
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Bloomberg Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BloombergUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private implicit val logger = LoggerFactory.getLogger(getClass)

  // DAO & services
  private val quoteService = new BloombergQuoteService()

  // internal variables
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val processor = new ConcurrentProcessor()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(clock: TradingClock) = !clock.isTradingActive

  /**
    * Executes the process
    * @param clock the given [[TradingClock trading clock]]
    */
  override def run(clock: TradingClock) = {
    val startTime = js.Date.now()
    val outcome = for {
      securities <- securitiesDAO.flatMap(_.findSymbolsIfEmpty("sector"))
      status <- processor.start(securities, ctx = ConcurrentContext(concurrency = 20), handler = new BulkUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef) = {
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
  }

}
