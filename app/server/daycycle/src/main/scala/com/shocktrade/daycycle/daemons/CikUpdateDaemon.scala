package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.dao.securities.SecurityRef
import com.shocktrade.concurrent.ConcurrentProcessor
import com.shocktrade.daycycle.{Daemon, SecuritiesUpdateHandler}
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
      outcome <- processor.start(securities, handler = new SecuritiesUpdateHandler {

        override val requested = securities.size

        override val logger = LoggerFactory.getLogger(getClass)

        override def updateSecurity(security: SecurityRef) = {
          for {
            response_? <- cikLookupService(security.symbol)
            result <- response_? match {
              case Some(response) => securitiesDAO.flatMap(_.updateCik(security.symbol, response.CIK))
              case None => Future.failed(die(s"No CIK response for symbol ${security.symbol}"))
            }
          } yield result
        }

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

}
