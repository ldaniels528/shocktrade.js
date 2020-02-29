package com.shocktrade.qualification

import com.shocktrade.qualification.ContestCloseOutEngine.ContestData
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon
import io.scalajs.npm.mongodb.{Db, UpdateWriteOpResultObject}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Contest Close-Out Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestCloseOutEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[Seq[UpdateWriteOpResultObject]] {
  private var lastRun: Option[js.Date] = None
  private val logger = LoggerFactory.getLogger(getClass)

  // get DAO references
  //private val contestDAO = ContestDAO()
  //private val portfolioDAO = dbFuture.map(_.getPortfolioUpdateDAO)

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param clock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(clock: TradingClock): Boolean = {
    val ready = lastRun.exists(clock.isTradingActive(_) && !clock.isTradingActive)
    lastRun = Option(new js.Date())
    ready
  }

  /**
    * Executes the process
    */
  override def run(clock: TradingClock): Future[Seq[UpdateWriteOpResultObject]] = {
    val startTime = js.Date.now()
    val outcome = closeExpireContests()
    outcome onComplete {
      case Success(results) =>
        val processedTime = js.Date.now() - startTime
        logger.log(s"${results.size} contest(s) were updated")
        logger.log("Process completed in %d msec", processedTime)
      case Failure(e) =>
        logger.error(s"Failed to process portfolio: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  def closeExpireContests(): Future[Seq[UpdateWriteOpResultObject]] = ???

  def closeOut(contest: ContestData): Future[Seq[UpdateWriteOpResultObject]] = ???

  /*
  def liquidatePositions(portfolio: PortfolioData, quotes: Map[String, PricingQuote]): Future[List[UpdateWriteOpResultObject]] = {
    Future.failed(new Exception())
    Future.sequence {
      for {
        positions <- portfolio.positions.toList
        portfolioId <- portfolio.portfolioID.toList
        position <- positions
        price <- determinePrice(position, quotes).toList
      } yield {
        portfolioDAO.flatMap(_.liquidatePosition(
          portfolioId,
          position,
          price,
          commission = Commissions.MARKET_COST,
          asOfTime = new js.Date()
        ))
      }
    }
  }

  def determinePrice(position: PositionData, quotes: Map[String, PricingQuote]): js.UndefOr[Double] = {
    for {
      symbol <- position.symbol
      quote <- quotes.get(symbol).orUndefined
      price <- quote.lastTrade
    } yield price
  }*/

}

object ContestCloseOutEngine {

  trait ContestData extends js.Object

}