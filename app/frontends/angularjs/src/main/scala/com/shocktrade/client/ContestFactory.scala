package com.shocktrade.client

import com.shocktrade.client.contest.{ContestService, PortfolioService}
import com.shocktrade.client.models.contest._
import com.shocktrade.common.models.contest.Portfolio
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Factory, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Graph Factory
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestFactory(@injected("ContestService") contestService: ContestService,
                     @injected("PortfolioService") portfolioService: PortfolioService) extends Factory {
  private val contestCache: js.Dictionary[Contest] = js.Dictionary()
  private val portfolioCache: js.Dictionary[Portfolio] = js.Dictionary()

  /**
   * Clears the contest and portfolio caches
   */
  def clear(): Unit = {
    contestCache.clear()
    portfolioCache.clear()
  }

  /**
   * Returns the contest object graph for the given contest ID
   * @param contestID the given contest ID
   * @return the [[Contest contest object graph]]
   */
  def findContest(contestID: String): Future[Contest] = {
    console.info(s"Retrieving contest graph for contest $contestID...")
    contestCache.get(contestID) match {
      case Some(contest) => Future.successful(contest)
      case None =>
        val outcome = buildContestGraph(contestID)
        outcome onComplete {
          case Success(contest) => contestCache(contestID) = contest
          case Failure(e) => console.error(s"findContest: contestID => '$contestID' ${e.getMessage}")
            e.printStackTrace()
        }
        outcome
    }
  }

  /**
   * Returns the contest and portfolio object graphs for the given contest ID and user ID
   * @param contestID the given contest ID
   * @param userID    the given user ID
   * @return the [[Contest contest object graph]] and the [[Portfolio portfolio object graph]]
   */
  def findContestAndPortfolio(contestID: String, userID: String): Future[(Contest, Portfolio)] = {
    for {
      contest <- findContest(contestID)
      portfolio <- findPortfolio(contestID, userID)
    } yield (contest, portfolio)
  }

  /**
   * Returns the portfolio object graph for the given contest ID and user ID
   * @param contestID the given contest ID
   * @param userID    the given user ID
   * @return the [[Portfolio portfolio object graph]]
   */
  def findPortfolio(contestID: String, userID: String): Future[Portfolio] = {
    console.info(s"Retrieving portfolio graph for contest $contestID, user $userID...")
    val key = s"$contestID.$userID"
    portfolioCache.get(key) match {
      case Some(portfolio) => Future.successful(portfolio)
      case None =>
        val outcome = buildPortfolioGraph(contestID, userID)
        outcome onComplete {
          case Success(portfolio) => portfolioCache(key) = portfolio
          case Failure(e: js.JavaScriptException) => console.error(s"findPortfolio: contestID => '$contestID' ${JSON.stringify(e.getMessage)}")
            e.printStackTrace()
        }
        outcome
    }
  }

  def markContestAsDirty(contestID: String): this.type = {
    contestCache.remove(contestID)
    this
  }

  def markPortfolioAsDirty(contestID: String, userID: String): this.type = {
    portfolioCache.remove(key = s"$contestID.$userID")
    this
  }

  private def buildContestGraph(contestID: String): Future[Contest] = {
    for {
      contest <- contestService.findContestByID(contestID)
      rankings <- contestService.findContestRankings(contestID)
      portfolios <- portfolioService.findPortfoliosByContest(contestID)
    } yield {
      new Contest(
        contestID = contest.data.contestID,
        name = contest.data.name,
        hostUserID = contest.data.hostUserID,
        startTime = contest.data.startTime,
        startingBalance = contest.data.startingBalance,
        expirationTime = contest.data.expirationTime,
        status = contest.data.status,
        timeOffset = contest.data.timeOffset,
        // portfolios & rankings
        portfolios = portfolios.data,
        rankings = rankings.data.sortBy(_.rankNum.getOrElse(Int.MaxValue)),
        // indicators
        friendsOnly = contest.data.friendsOnly,
        invitationOnly = contest.data.invitationOnly,
        levelCap = contest.data.levelCap,
        perksAllowed = contest.data.perksAllowed,
        robotsAllowed = contest.data.robotsAllowed)
    }
  }

  private def buildPortfolioGraph(contestID: String, userID: String): Future[Portfolio] = {
    for {
      portfolio <- portfolioService.findPortfolioByUser(contestID, userID)
      balance <- portfolioService.findPortfolioBalance(contestID, userID)
      orders <- portfolioService.findOrders(contestID, userID)
      positions <- portfolioService.findPositions(contestID, userID)
    } yield {
      new Portfolio(
        portfolioID = portfolio.data.portfolioID,
        contestID = portfolio.data.contestID,
        userID = portfolio.data.userID,
        username = portfolio.data.username,
        active = portfolio.data.active,
        funds = portfolio.data.funds,
        balance = balance.data,
        perks = portfolio.data.perks,
        orders = orders.data,
        positions = positions.data)
    }
  }

}
