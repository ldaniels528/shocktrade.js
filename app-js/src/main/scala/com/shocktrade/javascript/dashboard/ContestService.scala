package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Service, angular}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.models._
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Service
 * @author lawrence.daniels@gmail.com
 */
class ContestService($http: Http, toaster: Toaster) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def createContest(form: js.Dynamic) = {
    required("form", form)
    $http.put[Contest]("/api/contest", form)
  }

  def deleteContest(contestId: String) = {
    required("contestId", contestId)
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: String, playerInfo: js.Dynamic) = {
    required("contestId", contestId)
    required("playerInfo", playerInfo)
    $http.put[Contest](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.delete[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: String) = {
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

  def getContestByID(contestId: String) = {
    required("contestId", contestId)
    $http.get[Contest](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def getCashAvailable(contest: js.Dynamic, playerId: String) = {
    contest.participants.asArray[js.Dynamic].find(_.OID_?.contains(playerId)) map (_.cashAccount.cashFunds.as[Double]) getOrElse 0.0d
  }

  def getRankings(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Array[ParticipantRanking]](s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Array[Contest]](s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Array[Order]](s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Array[Position]](s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getPlayerRankings(contest: Contest, playerID: String) = {
    if (isDefined(contest) && isDefined(contest.name)) {
      // if the rankings have never been loaded ...
      if (!isDefined(contest.rankings)) {
        // create the rankings object: { participants: [], leader:null, player:null }
        contest.rankings = Rankings()

        console.log(s"Loading Contest Rankings for '${contest.name}'...")
        contest.OID_?.foreach { contestId =>
          getRankings(contestId) onComplete {
            case Success(participantRankings) =>
              console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
              contest.rankings.participants = participantRankings
              contest.rankings.leader = participantRankings.headOption.orNull
              contest.rankings.player = participantRankings.find(p => p.OID_?.contains(playerID) || p.facebookID == playerID).orNull
            case Failure(e) =>
              toaster.error("Error loading player rankings")
              console.error(s"Error loading player rankings: ${e.getMessage}")
              e.printStackTrace()
          }
        }
      }

      // if the rankings were loaded, but the player is not set
      else if (isDefined(contest.rankings) && !isDefined(contest.rankings.player)) {
        contest.rankings.player = contest.rankings.participants.find(p => p.OID_?.contains(playerID) || p.name == playerID || p.facebookID == playerID).orNull
      }

      contest.rankings
    }
    else makeNew[Rankings]
  }

  def getTotalInvestment(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contests/player/$playerId/totalInvestment")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(exposure: String, contestId: String, userID: String) = {
    required("exposure", exposure)
    required("contestId", contestId)
    required("userID", userID)
    $http.get[js.Array[js.Dynamic]](s"/api/charts/exposure/$exposure/$contestId/$userID")
  }

  def getChart(contestId: String, participantName: String, chartName: String) = {
    required("contestId", contestId)
    required("participantName", participantName)
    required("chartName", chartName)

    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestId/$participantName")
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

  def createOrder(contestId: String, playerId: String, order: js.Dynamic) = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("order", order)
    $http.put[js.Dynamic](s"/api/order/$contestId/$playerId", order)
  }

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

  def orderQuote(symbol: String) = {
    required("symbol", symbol)
    $http.get[js.Dynamic](s"/api/quotes/order/symbol/$symbol")
  }

}
