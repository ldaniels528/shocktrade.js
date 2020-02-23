package com.shocktrade.ingestion.daemons.eoddata

import com.shocktrade.ingestion.daemons.eoddata.EodDataCompanyUpdateDaemon._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.services.EodDataSecuritiesService
import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Full Market Update Daemon (supports AMEX, NASDAQ, NYSE and OTCBB)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class EodDataCompanyUpdateDaemon()(implicit ec: ExecutionContext) {
  private implicit val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  // DAO and service instances
  private val securitiesDAO = EodDataUpdateDAO()
  private val eodDataService = new EodDataSecuritiesService()

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
  def run(tradingClock: TradingClock): Future[Seq[EodDataStats]] = {
    val startTime = js.Date.now()
    val inputs = getInputPages
    val outcome = Future.sequence(inputs map { input =>
      logger.info(s"Scheduling ${input.exchange}/${input.letterCode}...")
      val procStartTime = js.Date.now()
      for {
        quotes <- eodDataService(input.exchange, input.letterCode)
        count <- securitiesDAO.updateAll(quotes)
      } yield new EodDataStats(input.exchange, input.letterCode.toString, count, js.Date.now() - procStartTime)
    })

    outcome onComplete {
      case Success(stats) =>
        logger.info(s"Process completed in ${(js.Date.now() - startTime) / 1000} seconds")
        stats.foreach { stat =>
          logger.info(JSON.stringify(stat))
        }
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  private def getInputPages: Seq[InputPages] = {
    for {
      exchange <- js.Array("NASDAQ", "NYSE", "AMEX", "OTCBB")
      letterCode <- 'A' to 'Z'
    } yield InputPages(exchange, letterCode)
  }

}

/**
 * Full Market Update Daemon
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object EodDataCompanyUpdateDaemon {

  /**
   * Represents an input object containing an exchange and letter code
   * @param exchange   the given exchange (e.g. "NYSE")
   * @param letterCode the given letter code; the first letter of the symbol (e.g. "A")
   */
  case class InputPages(exchange: String, letterCode: Char)

  /**
   * Represents an EOD Data processing statistic
   * @param exchange    the Security's exchange code
   * @param letterCode  the letter code ('A' to 'Z')
   * @param count       the number of records inserted or updated
   * @param processTime the processing time
   */
  class EodDataStats(val exchange: String,
                     val letterCode: String,
                     val count: Int,
                     val processTime: Double) extends js.Object

}
