package com.shocktrade.javascript.dashboard

import biz.enef.angulate.Service
import biz.enef.angulate.core.HttpService
import com.ldaniels528.angularjs.{CookieStore, Toaster}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Failure, Success}

/**
 * Contest Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ContestService($cookieStore: CookieStore, $http: HttpService, toaster: Toaster) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def createContest: js.Function = (form: js.Dynamic) => {
    required("form", form)
    $http.put[js.Dynamic]("/api/contest", form)
  }

  def deleteContest: js.Function = (contestId: String) => {
    required("contestId", contestId)
    deleteContest_@(contestId)
  }

  protected[javascript] def deleteContest_@(contestId: String) = {
    required("contestId", contestId)
    $http.delete(s"/api/contest/$contestId")
  }

  def joinContest: js.Function = (contestId: String, playerInfo: js.Dynamic) => joinContest_@(contestId, playerInfo)

  protected[javascript] def joinContest_@(contestId: String, playerInfo: js.Dynamic) = {
    required("contestId", contestId)
    required("playerInfo", playerInfo)
    $http.put[js.Dynamic](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest: js.Function = (contestId: String, playerId: String) => {
    required("contestId", contestId)
    required("playerId", playerId)
    quitContest_@(contestId, playerId)
  }

  protected[javascript] def quitContest_@(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.delete(s"/api/contest/$contestId/player/$playerId")
  }

  def startContest: js.Function = (contestId: String) => startContest_@(contestId)

  protected[javascript] def startContest_@(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests: js.Function = (searchOptions: js.Dynamic) => findContests_@(searchOptions)

  protected[javascript] def findContests_@(searchOptions: js.Dynamic) = {
    required("searchOptions", searchOptions)
    $http.post[js.Dynamic]("/api/contests/search", searchOptions)
  }

  def getContestByID: js.Function = (contestId: String) => getContestByID_@(contestId)

  protected[javascript] def getContestByID_@(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Dynamic](s"/api/contest/$contestId")
  }

  def getParticipantByID: js.Function = (contestId: String, playerId: String) => {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/player/$playerId")
  }

  def getCashAvailable: js.Function = { (contest: js.Dynamic, playerId: String) =>
    contest.participants.asArray[js.Dynamic].find(_.OID == playerId) map (_.cashAccount.cashFunds) getOrElse 0.0
  }

  def getRankings: js.Function = (contestId: String) => getRankings_@(contestId)

  protected[javascript] def getRankings_@(contestId: String) = {
    required("contestId", contestId)
    $http.get[js.Array[js.Dynamic]](s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID: js.Function = (playerId: String) => getContestsByPlayerID_@(playerId)

  def getContestsByPlayerID_@(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders: js.Function = (contestId: String, playerId: String) => {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions: js.Function = (contestId: String, playerId: String) => {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def findPlayerByID: js.Function = (contest: js.Dynamic, playerId: String) => findPlayerByID_@(contest, playerId) getOrElse JS()

  protected[javascript] def findPlayerByID_@(contest: js.Dynamic, playerId: String) = {
    if (isDefined(contest) && isDefined(contest.participants))
      contest.participants.asArray[js.Dynamic].find(_.OID == playerId)
    else
      None
  }

  def findPlayerByName: js.Function = (contest: js.Dynamic, playerName: String) => {
    required("contest", contest)
    required("playerName", playerName)
    contest.participants.asArray[js.Dynamic].find(_.name === playerName) getOrElse JS()
  }

  def getMarginMarketValue: js.Function = (contestId: String, playerId: String) => getMarginMarketValue_@(contestId, playerId)

  def getMarginMarketValue_@(contestId: String, playerId: String) = {
    required("contestId", contestId)
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contest/$contestId/margin/$playerId/marketValue")
  }

  def getPlayerRankings: js.Function = (contest: js.Dynamic, playerName: String) => getPlayerRankings_@(contest, playerName)

  protected[javascript] def getPlayerRankings_@(contest: js.Dynamic, playerName: String) = {
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
        getRankings_@(contest.OID) onComplete {
          case Success(participants) =>
            contest.rankings.participants = participants
            contest.rankings.leader = participants.headOption.orNull
            contest.rankings.player = participants.find(_.name === playerName).orNull
          case Failure(e) =>
            toaster.pop("error", "Error loading play rankings", null)
            e.printStackTrace()
        }
      }

      // if the rankings were loaded, but the player is not set
      else if (isDefined(contest.rankings) && !isDefined(contest.rankings.player)) {
        contest.rankings.player = contest.rankings.participants.asArray[js.Dynamic].find(_.name === playerName).orNull
      }

      contest.rankings
    }
    else JS()
  }

  def getTotalInvestment: js.Function = (playerId: String) => getTotalInvestment_@(playerId)

  protected[javascript] def getTotalInvestment_@(playerId: String) = {
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/contests/player/$playerId/totalInvestment")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Chat and Charts
  /////////////////////////////////////////////////////////////////////////////

  def getChart: js.Function = (contestId: String, participantName: String, chartName: String) => {
    required("contestId", contestId)
    required("participantName", participantName)
    required("chartName", chartName)

    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestId/$participantName")
  }

  def sendChatMessage: js.Function = (contestId: String, message: js.Dynamic) => sendChatMessage_@(contestId, message)

  protected[javascript] def sendChatMessage_@(contestId: String, message: js.Dynamic) = {
    required("contestId", contestId)
    required("message", message)
    $http.put[js.Dynamic](s"/api/contest/$contestId/chat", message)
  }


  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def createOrder: js.Function = (contestId: String, playerId: String, order: js.Dynamic) => {
    required("contestId", contestId)
    required("playerId", playerId)
    required("order", order)
    $http.put[js.Dynamic](s"/api/order/$contestId/$playerId", order)
  }

  def deleteOrder: js.Function = (contestId: String, playerId: String, orderId: String) => {
    required("contestId", contestId)
    required("playerId", playerId)
    required("orderId", orderId)
    $http.delete(s"/api/order/$contestId/$playerId/$orderId")
  }

  def getHeldSecurities: js.Function = (playerId: String) => {
    required("playerId", playerId)
    $http.get[js.Dynamic](s"/api/positions/$playerId")
  }

  def orderQuote: js.Function = (symbol: String) => {
    required("symbol", symbol)
    $http.get[js.Dynamic](s"/api/quotes/order/symbol/$symbol")
  }

}
