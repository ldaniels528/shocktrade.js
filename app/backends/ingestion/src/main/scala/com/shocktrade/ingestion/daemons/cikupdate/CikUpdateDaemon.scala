package com.shocktrade.ingestion.daemons.cikupdate

import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.services.CikLookupService
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * CIK Update Daemon
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class CikUpdateDaemon()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  // get the DAO and service
  private val cikUpdateDAO = CikUpdateDAO()
  private val cikLookupService = new CikLookupService()

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
  def run(clock: TradingClock)(implicit ec: ExecutionContext): Future[Int] = {
    val startTime = js.Date.now()
    val outcome = for {
      missingCiks <- cikUpdateDAO.findMissing.map(_.toSeq)
      processedCount <- Future.sequence(missingCiks map { missingCik =>
        for {
          response_? <- cikLookupService(missingCik.symbol)
          result <- response_? match {
            case Some(response) =>
              logger.info(s"Updating ${missingCik.exchange}:${response.symbol} with ${response.cikNumber}...")
              cikUpdateDAO.updateCik(new CikUpdateData(
                symbol = response.symbol,
                exchange = missingCik.exchange,
                cikNumber = response.cikNumber,
                name = response.name,
                mailingAddress = response.mailingAddress.map(_.mkString(", "))
              ))
            case None =>
              Future.failed(die(s"No CIK response for symbol ${missingCik.symbol}"))
          }
        } yield result
      }) map (_.sum)
    } yield processedCount

    outcome onComplete {
      case Success(count) =>
        logger.info(s"$count symbols updated in ${(js.Date.now() - startTime) / 1000} seconds")
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

}
