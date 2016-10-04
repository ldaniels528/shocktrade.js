package com.shocktrade.client.contest

import com.shocktrade.common.models.contest._
import com.shocktrade.client.QuoteCache
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.util.StringHelper._
import org.scalajs.angularjs.angular
import org.scalajs.dom.browser.console
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Contest Ranking Capability
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait ContestRankingCapability {

  def quoteCache: QuoteCache

  def portfolioService: PortfolioService

  def getContestRankings(contest: Contest, playerId: String)(implicit ec: ExecutionContext): Future[ContestRankings] = {
    console.log(s"Loading rankings for ${contest._id.orNull}")
    for {
      portfolios <- portfolioService.getPortfoliosByContest(contest._id.orNull)
      rankings <- getContestRankings(contest, playerId, portfolios)
    } yield rankings
  }

  def getContestRankings(contest: Contest, playerID: String, portfolios: js.Array[Portfolio])(implicit ec: ExecutionContext): Future[ContestRankings] = {
    getPortfolioRankings(contest, portfolios) map { participantRankings =>
      console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
      new ContestRankings(
        participants = participantRankings,
        leader = participantRankings.headOption.orUndefined,
        player = participantRankings.find(p => p._id.contains(playerID) || p.facebookID.contains(playerID)).orUndefined
      )
    }
  }

  def getPortfolioRankings(contest: Contest, portfolios: js.Array[Portfolio])(implicit ec: ExecutionContext) = {
    val promisedRankings = Future.sequence {
      portfolios.toSeq map { portfolio =>
        val player = contest.participants.toOption.flatMap(_.find(_.is(portfolio.playerID))).orUndefined
        val positions = portfolio.positions.toList.flatMap(_.toList)

        console.log(s"computing investment - ${angular.toJson(positions.toJSArray)}")
        computeInvestment(positions) map { totalInvestment =>
          val startingBalance = contest.startingBalance
          val cashFunds = portfolio.cashAccount.flatMap(_.cashFunds)
          val totalEquity = cashFunds.map(_ + totalInvestment)
          val gainLoss_% = for {bal <- startingBalance; equity <- totalEquity} yield 100 * ((equity - bal) / bal)

          new PortfolioRanking(
            _id = portfolio.playerID,
            facebookID = player.flatMap(_.facebookID),
            name = player.flatMap(_.name),
            rank = js.undefined,
            totalEquity = totalEquity,
            gainLoss = gainLoss_%)
        }
      }
    }

    // sort the rankings and add the position (e.g. "1st")
    promisedRankings map { rankings =>
      val myRankings = rankings.sortBy(-_.gainLoss.getOrElse(0d))
      myRankings.zipWithIndex foreach { case (ranking, index) =>
        ranking.rank = (index + 1) nth
      }
      js.Array(myRankings: _*)
    }
  }

  private def computeInvestment(positions: Seq[PositionLike])(implicit ec: ExecutionContext) = {
    Future.sequence {
      positions map { p =>
        val result = (for {
          symbol <- p.symbol
          quantity <- p.quantity
        } yield (symbol, quantity)) toOption

        val outcome = result match {
          case Some((symbol, quantity)) =>
            quoteCache.get(symbol) map { quote =>
              console.log(s"The quote ($symbol) was loaded")
              quote.lastTrade map (_ * quantity) toList
            }
          case None => Future.successful(Nil)
        }

        outcome.map(_.sum)
      }
    } map (_.sum)
  }

}
