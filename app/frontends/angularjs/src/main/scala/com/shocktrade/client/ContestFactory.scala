package com.shocktrade.client

import com.shocktrade.client.contest.{ContestService, PortfolioService}
import com.shocktrade.client.models.contest._
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.HttpResponse
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

  def getContest(contestID: String): Future[Contest] = {
    contestCache.get(contestID) match {
      case Some(contest) => Future.successful(contest)
      case None =>
        val outcome = getContestObjects(contestID) map { case (contest, messages, portfolios, rankings) =>
          new Contest(
            contestID = contest.data.contestID,
            name = contest.data.name,
            hostUserID = contest.data.hostUserID,
            startTime = contest.data.startTime,
            startingBalance = contest.data.startingBalance,
            status = contest.data.status,
            // chats
            messages = messages.data,
            // portfolios & rankings
            portfolios = portfolios.data,
            rankings = rankings.data,
            // indicators
            friendsOnly = contest.data.friendsOnly,
            invitationOnly = contest.data.invitationOnly,
            levelCap = contest.data.levelCap,
            perksAllowed = contest.data.perksAllowed,
            robotsAllowed = contest.data.robotsAllowed)
        }
        outcome onComplete {
          case Success(contest) => contestCache(contestID) = contest
          case Failure(e) => console.error(e.getMessage)
        }
        outcome
    }
  }

  def getPortfolio(contestID: String, userID: String): Future[Portfolio] = {
    val key = s"$contestID.$userID"
    portfolioCache.get(key) match {
      case Some(portfolio) => Future.successful(portfolio)
      case None =>
        val outcome = getPortfolioObjects(contestID, userID) map { case (portfolio, orders, positions) =>
          val graph = portfolio.data
          graph.closedOrders = orders.data
          graph.orders = orders.data
          graph.positions = positions.data
          graph.performance = js.undefined
          console.info(s"graph => ${JSON.stringify(graph)}")
          graph
        }
        outcome onComplete {
          case Success(portfolio) => portfolioCache(key) = portfolio
          case Failure(e) => console.error(e.getMessage)
        }
        outcome
    }
  }

  def logout(): Unit = {
    contestCache.clear()
    portfolioCache.clear()
  }

  def refreshMessages(contestID: String): Future[Unit] = {
    contestCache.get(contestID) map { contest =>
      contestService.getMessages(contestID) map { messages => contest.messages = messages.data }
    } getOrElse {
      Future.successful({})
    }
  }

  private def getContestObjects(contestID: String): Future[(HttpResponse[ContestSearchResultUI], HttpResponse[js.Array[ChatMessage]], HttpResponse[js.Array[Portfolio]], HttpResponse[js.Array[ContestRanking]])] = {
    for {
      contest <- contestService.findContestByID(contestID)
      messages <- contestService.getMessages(contestID)
      rankings <- contestService.findRankingsByContest(contestID)
      portfolios <- portfolioService.getPortfoliosByContest(contestID)
    } yield (contest, messages, portfolios, rankings)
  }

  private def getPortfolioObjects(contestID: String, userID: String): Future[(HttpResponse[Portfolio], HttpResponse[js.Array[Order]], HttpResponse[js.Array[Position]])] = {
    for {
      portfolio <- portfolioService.findPortfolio(contestID, userID)
      portfolioID = portfolio.data.portfolioID.orNull
      orders <- portfolioService.getOrders(portfolioID)
      positions <- portfolioService.getPositions(portfolioID)
    } yield (portfolio, orders, positions)
  }

}
