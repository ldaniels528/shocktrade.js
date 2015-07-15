package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Service, angular}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.models._
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success}

/**
 * Contest Service
 * @author lawrence.daniels@gmail.com
 */
class ContestService($http: Http, toaster: Toaster) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def deleteContest(contestId: UndefOr[String]) = {
    required("contestId", contestId)
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: UndefOr[String], playerInfo: js.Dynamic) = {
    required("contestId", contestId)
    required("playerInfo", playerInfo)
    $http.put[Contest](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: UndefOr[String], playerId: UndefOr[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.delete[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: UndefOr[String]) = {
    required("contestId", contestId)
    $http.get[Contest](s"/api/contest/$contestId/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions) = {
    required("searchOptions", searchOptions)
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: UndefOr[String]) = {
    required("contestId", contestId)
    $http.get[Contest](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: UndefOr[String], playerId: UndefOr[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def getCashAvailable(contest: Contest, playerId: UndefOr[String]) = {
    contest.participants.find(_.OID_?.exists(playerId.toOption.contains)) map (_.cashAccount.cashFunds) getOrElse 0.0d
  }

  def getRankings(contestId: UndefOr[String]) = {
    required("contestId", contestId)
    $http.get[js.Array[ParticipantRanking]](s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID(playerId: UndefOr[String]) = {
    required("playerId", playerId)
    $http.get[js.Array[Contest]](s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders(contestId: UndefOr[String], playerId: UndefOr[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Array[Order]](s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions(contestId: UndefOr[String], playerId: UndefOr[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Array[Position]](s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: UndefOr[String], playerId: UndefOr[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[MarginMarketValue](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getPlayerRankings(contest: Contest, playerID: UndefOr[String]): UndefOr[Rankings] = {
    if (isDefined(contest) && isDefined(contest.name)) {
      // if the rankings have never been loaded ...
      if (!isDefined(contest.rankings)) {
        // create the rankings object: { participants: [], leader:null, player:null }
        contest.rankings = Rankings()

        console.log(s"Loading Contest Rankings for '${contest.name}'...")
        for {
          contestId <- contest.OID_?
          userID <- playerID
        } {
          getRankings(contestId) onComplete {
            case Success(participantRankings) =>
              console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
              contest.rankings.foreach { rankings =>
                rankings.participants = participantRankings
                rankings.leader = participantRankings.headOption.orUndefined
                rankings.player = participantRankings.find(p => p.OID_?.contains(userID) || p.facebookID == userID).orUndefined
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
          userID <- playerID
        } {
          rankings.player = rankings.participants.find(p => p.OID_?.contains(userID) || p.facebookID == userID).orUndefined
        }
      }

      contest.rankings
    }

    else makeNew[Rankings]
  }

  def getTotalInvestment(playerID: UndefOr[String]) = {
    required("playerID", playerID)
    $http.get[js.Dynamic](s"/api/contests/player/$playerID/totalInvestment")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(contestID: UndefOr[String], playerID: UndefOr[String], exposure: UndefOr[String]) = {
    required("contestID", contestID)
    required("playerID", playerID)
    required("exposure", exposure)
    $http.get[js.Array[js.Dynamic]](s"/api/charts/exposure/$exposure/$contestID/$playerID")
  }

  def getChart(contestID: UndefOr[String], playerName: UndefOr[String], chartName: UndefOr[String]) = {
    required("contestID", contestID)
    required("playerName", playerName)
    required("chartName", chartName)

    // determine the chart type
    val chartType = if (chartName.exists(s => s == "gains" || s == "losses")) "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestID/$playerName")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Chat
  /////////////////////////////////////////////////////////////////////////////

  def sendChatMessage(contestId: String, message: Message) = {
    required("contestId", contestId)
    required("message", message)
    $http.put[js.Array[Message]](s"/api/contest/$contestId/chat", message)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def deleteOrder(contestId: String, playerId: String, orderId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("orderId", orderId)
    $http.delete[Contest](s"/api/order/$contestId/$playerId/$orderId")
  }

  def getHeldSecurities(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Array[String]](s"/api/positions/$playerId")
  }

}

trait MarginMarketValue extends js.Object {
  var _id: js.Dynamic = js.native
  var name: String = js.native
  var marginMarketValue: Double = js.native
}
