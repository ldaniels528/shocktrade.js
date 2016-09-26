package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.daycycle.Daemon
import com.shocktrade.services.NASDAQCompanyListService.NASDAQCompanyInfo
import com.shocktrade.services.{LoggerFactory, NASDAQCompanyListService}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Company List Update Daemon (supports AMEX, NASDAQ and NYSE)
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CompanyListUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private val logger = LoggerFactory.getLogger(getClass)

  // get DAO and service references
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val companyListService = new NASDAQCompanyListService()

  /**
    * Executes the process
    */
  def run(): Unit = {
    val startTime = js.Date.now()
    val outcome = for {
      amex <- getCompanyList("AMEX")
      nasdaq <- getCompanyList("NASDAQ")
      nyse <- getCompanyList("NYSE")
      results <- processCompanyList(amex ++ nasdaq ++ nyse)
    } yield results

    outcome onComplete {
      case Success(results) =>
        logger.log(s"Process completed in %d seconds", (js.Date.now() - startTime) / 1000)
        logger.info("records - nInserted: %d, nUpserted: %d, nMatched: %d, nModified: %d",
          results.nInserted, results.nUpserted, results.nMatched, results.nModified)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def processCompanyList(companies: Seq[NASDAQCompanyInfo]) = {
    logger.info(s"Saving ${companies.size} company information record(s)...")
    securitiesDAO.flatMap(_.updateCompanyInfo(companies).toFuture)
  }

  private def getCompanyList(exchange: String) = {
    loadCompanyList(exchange) map { companies =>
      logger.info(s"$exchange: Retrieved %d company information record(s)", companies.size)
      companies
    } recover { case e =>
      logger.error(s"$exchange: Failed to retrieve the company list: ${e.getMessage}")
      Nil
    }
  }

  private def loadCompanyList(exchange: String) = {
    exchange match {
      case "AMEX" => companyListService.amex()
      case "NASDAQ" => companyListService.nasdaq()
      case "NYSE" => companyListService.nyse()
      case other => Future.failed(die(s"Exchange '$exchange' is not recognized"))
    }
  }

}
