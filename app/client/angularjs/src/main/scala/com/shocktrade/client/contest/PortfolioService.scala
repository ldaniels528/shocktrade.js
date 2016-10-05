package com.shocktrade.client.contest

import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest._
import com.shocktrade.client.contest.PortfolioService.MarginMarketValue
import com.shocktrade.client.models.contest
import com.shocktrade.client.models.contest.{Contest, Order, Portfolio}
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.{Service, angular}
import org.scalajs.dom._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Portfolio Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PortfolioService($http: Http) extends Service {

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def cancelOrder(portfolioId: String, orderId: String) = {
    $http.delete[Portfolio](s"/api/portfolio/$portfolioId/order/$orderId")
  }

  def createOrder(portfolioId: String, order: NewOrderForm) = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioId/order", data = order)
  }

  def getOrders(portfolioId: String) = {
    $http.get[js.Array[Order]](s"/api/portfolio/$portfolioId/orders")
  }

  def getPositions(portfolioId: String) = {
    $http.get[js.Array[contest.Position]](s"/api/portfolio/$portfolioId/positions")
  }

  def getHeldSecurities(playerId: String) = {
    $http.get[js.Array[String]](s"/api/portfolio/$playerId/positions/symbols")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(contestID: String, playerID: String, exposure: String) = {
    $http.get[js.Array[js.Object]](s"/api/charts/exposure/$exposure/$contestID/$playerID")
  }

  def getChart(contestID: String, playerName: String, chartName: String) = {
    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestID/$playerName")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: String, playerId: String) = {
    $http.get[MarginMarketValue](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getRankings(contestId: String) = {
    $http.get[js.Array[PortfolioRanking]](s"/api/portfolios/contest/$contestId/rankings")
  }

  def getPlayerRankings(aContest: js.UndefOr[Contest], aPlayerID: js.UndefOr[String])(implicit ec: ExecutionContext): js.UndefOr[ContestRankings] = {
    for {
      contest <- aContest
      contestId <- contest._id
      playerID <- aPlayerID
    } yield {
      contest.rankings.toOption match {
        case Some(rankings) => rankings
        case None =>
          // create the rankings object: { participants: [], leader:null, player:null }
          val rankings = new ContestRankings()
          contest.rankings = rankings
          console.log(s"Loading Contest Rankings for '${contest.name}'...")

          // asynchronously retrieve the rankings
          getRankings(contestId) onComplete {
            case Success(participantRankings) =>
              console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
              contest.rankings.foreach { rankings =>
                rankings.participants = participantRankings
                rankings.leader = participantRankings.headOption.orUndefined
                rankings.player = participantRankings.find(p => p._id.contains(playerID) || p.facebookID.contains(playerID)).orUndefined
              }
            case Failure(e) =>
              console.error(s"Error loading player rankings: ${e.getMessage}")
              e.printStackTrace()
          }
          rankings
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Other
  /////////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves a portfolio by a contest ID and player ID
    * @param contestID the given contest ID
    * @param playerID  the given player ID
    * @return the promise of a [[Portfolio portfolio]]
    */
  def getPortfolioByPlayer(contestID: String, playerID: String) = {
    $http.get[Portfolio](s"/api/portfolio/contest/$contestID/player/$playerID")
  }

  /**
    * Retrieves a collection of portfolios by a contest ID
    * @param contestID the given contest ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByContest(contestID: String) = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/contest/$contestID")
  }

  /**
    * Retrieves a collection of portfolios by a player ID
    * @param playerID the given player ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByPlayer(playerID: String) = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/player/$playerID")
  }

  def getTotalInvestment(playerID: String) = {
    $http.get[TotalInvestment](s"/api/portfolios/player/$playerID/totalInvestment")
  }

}

/**
  * Portfolio Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioService {

  /**
    * Represents a Margin Market Value
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @js.native
  trait MarginMarketValue extends js.Object {
    var _id: js.UndefOr[String] = js.native
    var name: String = js.native
    var marginMarketValue: Double = js.native
  }

}
