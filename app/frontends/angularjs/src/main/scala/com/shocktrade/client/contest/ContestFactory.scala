package com.shocktrade.client.contest

import com.shocktrade.client.contest.models.{Contest, Portfolio}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Factory, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * Contest Graph Factory
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestFactory(@injected("ContestService") contestService: ContestService,
                     @injected("PortfolioService") portfolioService: PortfolioService) extends Factory {

  /**
   * Returns the contest object graph for the given contest ID
   * @param contestID the given contest ID
   * @return the [[Contest contest object graph]]
   */
  def findContest(contestID: String): Future[Contest] = {
    for {
      contest <- contestService.findContestByID(contestID)
      rankings <- contestService.findContestRankings(contestID)
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
        rankings = rankings.data.sortBy(_.rankNum.getOrElse(Int.MaxValue)),
        // indicators
        friendsOnly = contest.data.friendsOnly,
        invitationOnly = contest.data.invitationOnly,
        levelCap = contest.data.levelCap,
        perksAllowed = contest.data.perksAllowed,
        robotsAllowed = contest.data.robotsAllowed)
    }
  }

  /**
   * Returns the portfolio object graph for the given contest ID and user ID
   * @param contestID the given contest ID
   * @param userID    the given user ID
   * @return the [[Portfolio portfolio object graph]]
   */
  def findPortfolio(contestID: String, userID: String): Future[Portfolio] = {
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
        totalXP = portfolio.data.totalXP,
        balance = balance.data,
        perks = portfolio.data.perks,
        orders = orders.data,
        positions = positions.data,
        closedTime = portfolio.data.closedTime)
    }
  }

}
