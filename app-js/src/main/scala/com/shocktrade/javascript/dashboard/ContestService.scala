package com.shocktrade.javascript.dashboard

import com.greencatsoft.angularjs.core.{HttpService, Log}
import com.greencatsoft.angularjs.{Factory, Service, injectable}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.ServiceSupport
import prickle.Unpickle

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport

/**
 * Contest Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("ContestService")
class ContestService($http: HttpService, $log: Log, toaster: Toaster) extends Service with ServiceSupport {

  ///////////////////////////////////////////////////////////////
  //          Contest C.R.U.D.
  ///////////////////////////////////////////////////////////////
  
  def createContest(form: String) = $http.put("/api/contest", form)
  
  def deleteContest(contestId: String) = $http.delete("/api/contest/" + contestId)
  
  def joinContest(contestId: String, playerInfo: String) = $http.put(s"/api/contest/$contestId/player", playerInfo)
 
  def quitContest(contestId: String, playerId: String) = $http.delete(s"/api/contest/$contestId/player/$playerId")

  def startContest(contestId: String) = $http.get(s"/api/contest/$contestId/start")

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: String) = {
    if (searchOptions == null) throw new IllegalStateException("SearchOptions is null or undefined")
    else $http.post("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: String) = {
    if (contestId == null) throw new IllegalStateException("getContestByID: Contest ID is null or undefined")
    else $http.get(s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String) = {
    if (contestId == null) throw new IllegalStateException("getParticipantByID: Contest ID is null or undefined")
    else if (playerId == null) throw new IllegalStateException("getParticipantByID: Player ID is null or undefined")
    else $http.get("/api/contest/$contestId/player/$playerId")
  }

  /*
  def getCashAvailable(contest: String, playerId: String) = {
    if (contest == null) 0.00
    else {
      val player = findPlayerByID(contest, playerId)
      if (player != null) player.cashAccount.cashFunds else 0.00
    }
  }*/

  def getRankings(contestId: String) = {
    if (contestId == null) throw new IllegalStateException("getRankings: Contest ID is null or undefined")
    else $http.get(s"/api/contest/$contestId/rankings")
  }

  def getContestsByPlayerID(playerId: String) = {
    if (playerId == null) throw new IllegalStateException("getContestsByPlayerID: Player ID is null or undefined")
    else $http.get(s"/api/contests/player/$playerId")
  }

  def getEnrichedOrders(contestId: String, playerId: String) = {
    if (contestId == null) throw new IllegalStateException("getEnrichedOrders: Contest ID is null or undefined")
    else if (playerId == null) throw new IllegalStateException("getEnrichedOrders: Player ID is null or undefined")
    else $http.get(s"/api/contest/$contestId/orders/$playerId")
  }

  def getEnrichedPositions(contestId: String, playerId: String) = {
    if (contestId == null) throw new IllegalStateException("getEnrichedPositions: Contest ID is null or undefined")
    else if (playerId == null) throw new IllegalStateException("getEnrichedPositions: Player ID is null or undefined")
    else $http.get(s"/api/contest/$contestId/positions/$playerId")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getTotalInvestment(playerId: String) = flatten {
    if (playerId == null) throw new IllegalStateException("getTotalInvestment: Player ID is null or undefined")
    else {
      val future: Future[js.Any] = $http.get(s"/api/contests/player/$playerId/totalInvestment")
      future
        .map(JSON.stringify(_))
        .map(Unpickle[Double].fromString(_))
    }
  }

  /*
  def findPlayerByID(contest: String, playerId: String) = {
    val participants: js.Array = if (contest != null) contest.participants else js.Array()
    participants.filter(_.id == playerId).headOption
  }

  def findPlayerByName(contest: String, playerName: String) {
    val participants: js.Array = if (contest != null) contest.participants else js.Array()
    participants.filter(_.name == playerName).headOption
  }

  def getPlayerRankings(contest: String, playerName: String) = {
    if (contest == null || contest.name == null) js.Array()
    else {
      // if the rankings have never been loaded ...
      if (contest.rankings === undefined) {
        contest.rankings = {}
        $log.info(s"Loading Contest Rankings for ${contest.name}...")
        getRankings(contest.OID()).onSuccess {
          case Success(participants) =>
            contest.rankings.participants = participants
            if (participants.length) {
              contest.rankings.leader = participants[ 0]
              contest.rankings.player = if (playerName != null) findPlayerByName(contest.rankings, playerName) else null
            }
          case Failure(e) =>
            toaster.pop("error", "Error!", "Error loading play rankings")
            $log.error(response.error)
        }
      }

      // if the rankings were loaded, but the player is not set
      else if (playerName != null && contest.rankings.player == null) {
        contest.rankings.player = if (playerName != null) findPlayerByName(contest.rankings, playerName) else null
      }

      contest.rankings
    }
  }*/
 
  def getMarginMarketValue(contestId: String, playerId: String) = $http.get(s"/api/contest/$contestId/margin/$playerId/marketValue")

  /////////////////////////////////////////////////////////////////////////////
  //			Miscellaneous
  /////////////////////////////////////////////////////////////////////////////

  def getChart(contestId: String, participantName: String, chartName: String) = {
    // build the appropriate URL
    val uriString = if (chartName == "gains" || chartName == "losses")
      s"/api/charts/performance/$chartName/$contestId/$participantName"
    else
      s"/api/charts/exposure/$chartName/$contestId/$participantName"

    // load the chart representing the securities
    $http.get(uriString)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Chat
  /////////////////////////////////////////////////////////////////////////////
 
  def sendChatMessage(contestId: String, message: String) = $http.put(s"/api/contest/$contestId/chat", message)

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def createOrder(contestId: String, playerId: String, order: String) = $http.put(s"/api/order/$contestId/$playerId", order)

  def deleteOrder(contestId: String, playerId: String, orderId: String) = $http.delete(s"/api/order/$contestId/$playerId/$orderId")

  def getHeldSecurities(playerId: String) = $http.get(s"/api/positions/$playerId")

  def orderQuote(symbol: String) = {
    $log.info(s"Loading symbol $symbol...")
    val promise = $http.get(s"/api/quotes/order/symbol/$symbol")
    promise.foreach { quote =>
      /* TODO
      if (quote.symbol) {
        $log.info(s"Setting lastSymbol as ${quote.symbol}")
        QuoteService.lastSymbol = quote.symbol
      }*/
    }
    promise
  }

}

@injectable("ContestService")
class ContestServiceFactory(http: HttpService, log: Log, toaster: Toaster) extends Factory[ContestService] {

  override def apply(): ContestService = new ContestService(http, log, toaster)

}
