package com.shocktrade.javascript.dashboard

import com.ldaniels528.javascript.angularjs.core.{Http, Service}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Failure, Success}

/**
 * Contest Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ContestService($http: Http, toaster: Toaster) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def createContest(form: js.Dynamic) = {
    required("form", form)
    $http.put[js.Dynamic]("/api/contest", form)
  }

  def deleteContest(contestId: String) = {
    required("contestId", contestId)
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: String, playerInfo: js.Dynamic) = {
    required("contestId", contestId)
    required("playerInfo", playerInfo)
    $http.put[js.Dynamic](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.delete[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: js.Dynamic) = {
    required("searchOptions", searchOptions)
    $http.post[js.Array[js.Dynamic]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Dynamic](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def getCashAvailable(contest: js.Dynamic, playerId: String) = {
    contest.participants.asArray[js.Dynamic].find(_.OID == playerId) map (_.cashAccount.cashFunds.as[Double]) getOrElse 0.0d
  }

  def getRankings(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Array[js.Dynamic]](s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Array[js.Dynamic]](s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getPlayerRankings(contest: js.Dynamic, playerID: String) = {
    if (isDefined(contest) && isDefined(contest.name)) {
      // if the rankings have never been loaded ...
      if (!isDefined(contest.rankings)) {
        // create the rankings object: { participants: [], leader:null, player:null }
        contest.rankings = JS(
          participants = js.Array[js.Dynamic](),
          leader = null,
          player = null
        )
        g.console.log(s"Loading Contest Rankings for '${contest.name}'...")
        getRankings(contest.OID) onComplete {
          case Success(participants) =>
            contest.rankings.participants = participants
            contest.rankings.leader = participants.headOption.orNull
            contest.rankings.player = participants.find(p => p.OID == playerID || p.name === playerID || p.facebookID === playerID).orNull
          case Failure(e) =>
            toaster.error("Error loading play rankings")
            e.printStackTrace()
        }
      }

      // if the rankings were loaded, but the player is not set
      else if (isDefined(contest.rankings) && !isDefined(contest.rankings.player)) {
        contest.rankings.player = contest.rankings.participants.asArray[js.Dynamic].find(p => p.OID == playerID || p.name === playerID || p.facebookID === playerID).orNull
      }

      contest.rankings
    }
    else JS()
  }

  def getTotalInvestment(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contests/player/$playerId/totalInvestment")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Chat and Charts
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

  def sendChatMessage(contestId: String, message: js.Dynamic) = {
    required("contestId", contestId)
    required("message", message)
    $http.put[js.Array[js.Dynamic]](s"/api/contest/$contestId/chat", message)
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
    $http.delete[js.Dynamic](s"/api/order/$contestId/$playerId/$orderId")
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
