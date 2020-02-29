package com.shocktrade.qualification

import com.shocktrade.server.common.{LoggerFactory, TradingClock}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Qualification Module (CQM)
 * @param clock the implicit [[TradingClock trading clock]]
 * @param ec    the implicit [[ExecutionContext]]
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestQualificationModule(implicit clock: TradingClock, ec: ExecutionContext) {
  // get DAO and service references
  private val logger = LoggerFactory.getLogger(getClass)
  private val qualificationDAO = QualificationDAO()

  // internal fields
  private var lastRun: js.Date = new js.Date()

  /**
   * Indicates whether the daemon is eligible to be executed
   * @return true, if the daemon is eligible to be executed
   */
  def isReady: Boolean = clock.isTradingActive || clock.isTradingActive(lastRun)

  /**
   * Executes the process
   */
  def execute(marketClosed: Boolean): Future[Int] = qualificationDAO.doQualification()

  /**
   * Executes the process
   */
  def run(): Unit = {
    if (isReady || clock.isTradingActive(lastRun)) {
      logger.info("Starting the Contest Qualification Module...")

      // capture the time as the last run time
      val startTime = new js.Date()

      // perform the qualification
      execute(marketClosed = clock.isTradingActive(lastRun)) onComplete {
        case Success(count) =>
          logger.info(s"$count order(s) updated.")
        case Failure(e) =>
          logger.error(s"CQM failure: ${e.getMessage}", e)
      }

      // capture the time as the last run time
      lastRun = startTime
    }
  }

}
