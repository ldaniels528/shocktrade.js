package com.shocktrade.qualification

import com.shocktrade.common.Commissions
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon
import com.shocktrade.server.dao.contest.PortfolioUpdateDAO._
import com.shocktrade.server.dao.contest._
import com.shocktrade.server.facade.{PricingFacade, PricingQuote}
import io.scalajs.npm.mongodb.{Db, UpdateWriteOpResultObject}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Contest Close-Out Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestCloseOutEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[Seq[UpdateWriteOpResultObject]] {
  private var lastRun: Option[js.Date] = None
  private val logger = LoggerFactory.getLogger(getClass)

  // get DAO references
  private val contestDAO = ContestDAO()
  private val portfolioDAO = dbFuture.map(_.getPortfolioUpdateDAO)

  // get the facade references
  private val pricingFacade = new PricingFacade(dbFuture)

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

  def closeExpireContests(): Future[Seq[UpdateWriteOpResultObject]] = for {
    contests <- contestDAO.findActiveContests.map(_.toSeq)
    w <- Future.sequence(contests map closeOut).map(_.flatten)
  } yield w

  def closeOut(contest: ContestData): Future[Seq[UpdateWriteOpResultObject]] = {
    contest._id.map(_.toHexString()).toOption match {
      case Some(contestId) =>
        for {
          portfolios <- portfolioDAO.flatMap(_.findByContest(contestId).map(_.toSeq))
          symbols = portfolios.flatMap(_.positions.getOrElse(emptyArray).flatMap(_.symbol.toOption)).distinct
          quotes <- pricingFacade.findQuotes(symbols)
          quoteMap = Map(quotes.map(q => q.symbol -> q): _*)
          w <- Future.sequence(portfolios.map(liquidatePositions(_, quoteMap))).map(_.flatten)
        } yield w
      case None =>
        Future.successful(Nil)
    }
  }

  def liquidatePositions(portfolio: PortfolioData, quotes: Map[String, PricingQuote]): Future[List[UpdateWriteOpResultObject]] = Future.sequence {
    for {
      positions <- portfolio.positions.toList
      portfolioId <- portfolio._id.toList
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

  def determinePrice(position: PositionData, quotes: Map[String, PricingQuote]): js.UndefOr[Double] = {
    for {
      symbol <- position.symbol
      quote <- quotes.get(symbol).orUndefined
      price <- quote.lastTrade
    } yield price
  }

}

