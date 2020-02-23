package com.shocktrade.ingestion.daemons.nasdaq

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.services.NASDAQCompanyListService
import com.shocktrade.server.services.NASDAQCompanyListService.NASDAQCompanyInfo
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * NASDAQ Company List Update Daemon (supports AMEX, NASDAQ and NYSE)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NASDAQCompanyListUpdateDaemon()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  // get DAO and service references
  private val nasdaqDAO = NASDAQCompanyListUpdateDAO()
  private val companyListService = new NASDAQCompanyListService()

  /**
   * Indicates whether the daemon is eligible to be executed
   * @param tradingClock the given [[TradingClock trading clock]]
   * @return true, if the daemon is eligible to be executed
   */
  def isReady(tradingClock: TradingClock): Boolean = !tradingClock.isTradingActive

  /**
   * Executes the process
   * @param tradingClock the given [[TradingClock trading clock]]
   */
  def run(tradingClock: TradingClock): Future[Int] = {
    val startTime = js.Date.now()
    val outcome = for {
      amex <- getCompanyList("AMEX")
      amexCount <- processCompanyList(amex)

      nasdaq <- getCompanyList("NASDAQ")
      nasdaqCount <- processCompanyList(nasdaq)

      nyse <- getCompanyList("NYSE")
      nyseCount <- processCompanyList(nyse)

    } yield amexCount + nasdaqCount + nyseCount

    outcome onComplete {
      case Success(count) =>
        logger.info(s"Updated $count records in ${(js.Date.now() - startTime) / 1000} seconds")
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  private def processCompanyList(companies: Seq[NASDAQCompanyInfo]): Future[Int] = {
    logger.info(s"Saving ${companies.size} company information record(s)...")
    Future.sequence(companies map { data =>
      logger.info(s"Updating NASDAQ company list for ${data.exchange}:${data.symbol}...")
      nasdaqDAO.updateCompanyList(
        new NASDAQCompanyData(
          symbol = data.symbol,
          exchange = data.exchange,
          companyName = data.name,
          lastTrade = data.lastSale,
          marketCap = data.marketCap,
          ADRTSO = data.ADRTSO,
          IPOyear = data.IPOyear,
          sector = data.sector,
          industry = data.industry,
          summary = data.summary,
          quote = data.quote
        ))
    }) map (_.sum)
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
