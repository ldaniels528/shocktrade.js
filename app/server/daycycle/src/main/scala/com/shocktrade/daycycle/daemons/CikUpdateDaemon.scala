package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.SecurityRef
import com.shocktrade.concurrent.daemon.{BulkConcurrentTaskUpdateHandler, Daemon}
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentProcessor}
import com.shocktrade.services.{CikLookupService, LoggerFactory}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * CIK Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CikUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private val logger = LoggerFactory.getLogger(getClass)

  // get the DAO and service
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val cikLookupService = new CikLookupService()

  // internal variables
  private val processor = new ConcurrentProcessor()

  /**
    * Executes the process
    */
  def run(): Unit = {
    val startTime = js.Date.now()
    val outcome = for {
      securities <- securitiesDAO.flatMap(_.findSymbolsForCikUpdate())
      status <- processor.start(securities, concurrency = 15, handler = new BulkConcurrentTaskUpdateHandler[SecurityRef](securities.size) {
        logger.info(s"Scheduling ${securities.size} securities for CIK processing...")

        override def onNext(ctx: ConcurrentContext, security: SecurityRef) = {
          for {
            response_? <- cikLookupService(security.symbol)
            result <- response_? match {
              case Some(response) => securitiesDAO.flatMap(_.updateCik(security.symbol, response.CIK))
              case None => Future.failed(die(s"No CIK response for symbol ${security.symbol}"))
            }
          } yield (1, result)
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
