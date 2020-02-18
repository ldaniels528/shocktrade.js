package com.shocktrade.daycycle.daemons

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon
import com.shocktrade.server.concurrent.bulk.BulkUpdateOutcome
import com.shocktrade.server.concurrent.bulk.BulkUpdateOutcome._
import com.shocktrade.server.dao.securities.SecuritiesUpdateDAO
import com.shocktrade.server.services.NASDAQCompanyListService
import com.shocktrade.server.services.NASDAQCompanyListService.NASDAQCompanyInfo
import io.scalajs.npm.mongodb.{BulkWriteOpResultObject, Db}
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Company List Update Daemon (supports AMEX, NASDAQ and NYSE)
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NADSAQCompanyUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[BulkUpdateOutcome] {
  private val logger = LoggerFactory.getLogger(getClass)

  // get DAO and service references
  private val securitiesDAO = dbFuture.map(SecuritiesUpdateDAO.apply)
  private val companyListService = new NASDAQCompanyListService()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param tradingClock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(tradingClock: TradingClock): Boolean = !tradingClock.isTradingActive

  /**
    * Executes the process
    * @param tradingClock the given [[TradingClock trading clock]]
    */
  override def run(tradingClock: TradingClock): Future[BulkUpdateOutcome] = {
    val startTime = js.Date.now()
    val outcome = for {
      amex <- getCompanyList("AMEX")
      nasdaq <- getCompanyList("NASDAQ")
      nyse <- getCompanyList("NYSE")
      results <- processCompanyList(amex ++ nasdaq ++ nyse)
    } yield results.toBulkWrite

    outcome onComplete {
      case Success(stats) =>
        logger.info(s"$stats in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  private def processCompanyList(companies: Seq[NASDAQCompanyInfo]): Future[BulkWriteOpResultObject] = {
    logger.info(s"Saving ${companies.size} company information record(s)...")
    securitiesDAO.flatMap(_.updateCompanyInfo(companies).toFuture)
  }

  private def getCompanyList(exchange: String): Future[Seq[NASDAQCompanyInfo]] = {
    loadCompanyList(exchange) map { companies =>
      logger.info(s"$exchange: Retrieved %d company information record(s)", companies.size)
      companies
    } recover { case e =>
      logger.error(s"$exchange: Failed to retrieve the company list: ${e.getMessage}")
      Nil
    }
  }

  private def loadCompanyList(exchange: String): Future[Seq[NASDAQCompanyInfo]] = {
    exchange match {
      case "AMEX" => companyListService.amex()
      case "NASDAQ" => companyListService.nasdaq()
      case "NYSE" => companyListService.nyse()
      case other => Future.failed(die(s"Exchange '$exchange' is not recognized"))
    }
  }

}
