package com.shocktrade.daycycle

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.SecurityRef
import com.shocktrade.daycycle.CikUpdateProcess.Outcome
import com.shocktrade.services.ConcurrentProcessor.{ConcurrentContext, TaskHandler}
import com.shocktrade.services.{CikLookupService, ConcurrentProcessor, LoggerFactory}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.{Db, UpdateWriteOpResultObject}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * CIK Update Process
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CikUpdateProcess(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
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
      outcome <- processor.start(securities, new TaskHandler[SecurityRef, UpdateWriteOpResultObject, Outcome] {
        val requested = securities.size
        var successes = 0
        var failures = 0

        logger.info(s"Scheduling $requested securities for CIK updates...")

        @inline def processed = successes + failures

        override def onNext(ctx: ConcurrentContext[SecurityRef], security: SecurityRef) = updateSecurity(security)

        override def onSuccess(ctx: ConcurrentContext[SecurityRef], result: UpdateWriteOpResultObject) = {
          successes += 1
          val count = processed
          if (count % 100 == 0 || count == requested) {
            logger.info(s"Processed $count securities (successes: $successes, failures: $failures) so far...")
          }
        }

        override def onFailure(ctx: ConcurrentContext[SecurityRef], cause: Throwable) = failures += 1

        override def onComplete(ctx: ConcurrentContext[SecurityRef]) = Outcome(processed, successes, failures)
      }, concurrency = 15)
    } yield outcome

    outcome onComplete {
      case Success(results) =>
        logger.log(s"Process completed in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def updateSecurity(security: SecurityRef) = {
    for {
      response_? <- cikLookupService(security.symbol)
      result <- response_? match {
        case Some(response) => securitiesDAO.flatMap(_.updateCik(security.symbol, response.CIK))
        case None => Future.failed(die(s"No CIK response for symbol ${security.symbol}"))
      }
    } yield result
  }

}

/**
  * CIK Update Process Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object CikUpdateProcess {

  /**
    * Processing outcome
    */
  case class Outcome(processed: Int, successes: Int, failures: Int)

}