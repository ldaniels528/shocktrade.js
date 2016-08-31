package com.shocktrade.javascript.dashboard

import com.shocktrade.javascript.dashboard.ContestService.{ContestHost, MarginMarketValue}
import com.shocktrade.javascript.forms.PlayerInfoForm
import com.shocktrade.javascript.models.contest._
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Service, angular}
import org.scalajs.dom.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Contest Service
  * @author lawrence.daniels@gmail.com
  */
class ContestService($http: Http, toaster: Toaster) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def deleteContest(contestId: String) = {
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: String, playerInfo: PlayerInfoForm) = {
    $http.put[Contest](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: String, playerId: String) = {
    $http.delete[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: String) = {
    $http.get[Contest](s"/api/contest/$contestId/start")
  }

  def updateContestHost(contestId: String, host: String) = {
    $http.post[js.Dynamic](s"/api/contest/$contestId/host", new ContestHost(host))
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions) = {
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: String) = {
    $http.get[Contest](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String) = {
    $http.get[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def getCashAvailable(contest: Contest, playerId: String) = {
    val participant = contest.participants.flatMap(_.find(_._id.contains(playerId)).orUndefined)
    participant.flatMap(_.cashAccount.flatMap(_.cashFunds)) getOrElse 0.0d
  }

  def getRankings(contestId: String) = {
    $http.get[js.Array[ParticipantRanking]](s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID(playerId: String) = {
    $http.get[js.Array[Contest]](s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders(contestId: String, playerId: String) = {
    $http.get[js.Array[Order]](s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions(contestId: String, playerId: String) = {
    $http.get[js.Array[Position]](s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: String, playerId: String) = {
    $http.get[MarginMarketValue](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getPlayerRankings(contest: Contest, playerID: String): js.UndefOr[Rankings] = {
    if (isDefined(contest) && isDefined(contest.name)) {
      // if the rankings have never been loaded ...
      if (!isDefined(contest.rankings)) {
        // create the rankings object: { participants: [], leader:null, player:null }
        contest.rankings = new Rankings()

        console.log(s"Loading Contest Rankings for '${contest.name}'...")
        for {
          contestId <- contest._id
          userID = playerID
        } {
          getRankings(contestId) onComplete {
            case Success(participantRankings) =>
              console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
              contest.rankings.foreach { rankings =>
                rankings.participants = participantRankings
                rankings.leader = participantRankings.headOption.orUndefined
                rankings.player = participantRankings.find(p => p._id.contains(userID) || p.facebookID.contains(userID)).orUndefined
              }
            case Failure(e) =>
              toaster.error("Error loading player rankings")
              console.error(s"Error loading player rankings: ${e.getMessage}")
              e.printStackTrace()
          }
        }
      }

      // if the rankings were loaded, but the player is not set
      else if (contest.rankings.nonEmpty && contest.rankings.exists(_.player.isEmpty)) {
        for {
          rankings <- contest.rankings
          userID = playerID
        } {
          rankings.player = rankings.participants.find(p => p._id.contains(userID) || p.facebookID.contains(userID)).orUndefined
        }
      }

      contest.rankings
    }

    else New[Rankings]
  }

  def getTotalInvestment(playerID: String) = {
    $http.get[js.Dynamic](s"/api/contests/player/$playerID/totalInvestment")
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
  //			Chat
  /////////////////////////////////////////////////////////////////////////////

  def sendChatMessage(contestId: String, message: Message) = {
    $http.put[js.Array[Message]](s"/api/contest/$contestId/chat", message)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def deleteOrder(contestId: String, playerId: String, orderId: String) = {
    $http.delete[Contest](s"/api/order/$contestId/$playerId/$orderId")
  }

  def getHeldSecurities(playerId: String) = {
    $http.get[js.Array[String]](s"/api/positions/$playerId")
  }

}

/**
  * Contest Service Companion
  * @author lawrence.daniels@gmail.com
  */
object ContestService {

  /**
    * Contest Host
    * @param host the given host name
    */
  @ScalaJSDefined
  class ContestHost(val host: String) extends js.Object

  /**
    * Margin Market Value
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait MarginMarketValue extends js.Object {
    var _id: js.UndefOr[String] = js.native
    var name: String = js.native
    var marginMarketValue: Double = js.native
  }

}