package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Service, angular}
import com.shocktrade.javascript.models._
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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

  def deleteContest(contestId: BSONObjectID) = {
    $http.delete[js.Dynamic](s"/api/contest/${contestId.$oid}")
  }

  def joinContest(contestId: BSONObjectID, playerInfo: js.Dynamic) = {
    $http.put[Contest](s"/api/contest/${contestId.$oid}/player", playerInfo)
  }

  def quitContest(contestId: BSONObjectID, playerId: BSONObjectID) = {
    $http.delete[Contest](s"/api/contest/${contestId.$oid}/player/${playerId.$oid}")
  }

  def startContest(contestId: BSONObjectID) = {
    $http.get[Contest](s"/api/contest/${contestId.$oid}/start")
  }

  def updateContestHost(contestId: BSONObjectID, host: String) = {
    $http.post[js.Dynamic](s"/api/contest/${contestId.$oid}/host", js.Dynamic.literal(host = host))
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions) = {
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: BSONObjectID) = {
    $http.get[Contest](s"/api/contest/${contestId.$oid}")
  }

  def getParticipantByID(contestId: BSONObjectID, playerId: BSONObjectID) = {
    $http.get[js.Dynamic](s"/api/contest/${contestId.$oid}/player/${playerId.$oid}")
  }

  def getCashAvailable(contest: Contest, playerId: BSONObjectID) = {
    contest.participants.find(p => BSONObjectID.isEqual(p._id, playerId)) map (_.cashAccount.cashFunds) getOrElse 0.0d
  }

  def getRankings(contestId: BSONObjectID) = {
    $http.get[js.Array[ParticipantRanking]](s"/api/contest/${contestId.$oid}/rankings")
  }

  def getContestsByPlayerID(playerId: BSONObjectID) = {
    $http.get[js.Array[Contest]](s"/api/contests/player/${playerId.$oid}")
  }

  def getEnrichedOrders(contestId: BSONObjectID, playerId: BSONObjectID) = {
    $http.get[js.Array[Order]](s"/api/contest/${contestId.$oid}/orders/${playerId.$oid}")
  }

  def getEnrichedPositions(contestId: BSONObjectID, playerId: BSONObjectID) = {
    $http.get[js.Array[Position]](s"/api/contest/${contestId.$oid}/positions/${playerId.$oid}")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Participants
  /////////////////////////////////////////////////////////////////////////////

  def getMarginMarketValue(contestId: BSONObjectID, playerId: BSONObjectID) = {
    $http.get[MarginMarketValue](s"/api/contest/${contestId.$oid}/margin/${playerId.$oid}/marketValue")
  }

  def getPlayerRankings(contest: Contest, playerID: BSONObjectID): UndefOr[Rankings] = {
    if (isDefined(contest) && isDefined(contest.name)) {
      // if the rankings have never been loaded ...
      if (!isDefined(contest.rankings)) {
        // create the rankings object: { participants: [], leader:null, player:null }
        contest.rankings = Rankings()

        console.log(s"Loading Contest Rankings for '${contest.name}'...")
        for {
          contestId <- contest._id.toOption
          userID = playerID
        } {
          getRankings(contestId) onComplete {
            case Success(participantRankings) =>
              console.log(s"participantRankings = ${angular.toJson(participantRankings)}")
              contest.rankings.foreach { rankings =>
                rankings.participants = participantRankings
                rankings.leader = participantRankings.headOption.orUndefined
                rankings.player = participantRankings.find(p => p._id.exists(_.$oid.contains(userID)) || p.facebookID == userID.$oid).orUndefined
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
          rankings.player = rankings.participants.find(p => BSONObjectID.isEqual(p._id, userID) || p.facebookID == userID.$oid).orUndefined
        }
      }

      contest.rankings
    }

    else New[Rankings]
  }

  def getTotalInvestment(playerID: BSONObjectID) = {
    $http.get[js.Dynamic](s"/api/contests/player/${playerID.$oid}/totalInvestment")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(contestID: BSONObjectID, playerID: BSONObjectID, exposure: String) = {
    $http.get[js.Array[js.Object]](s"/api/charts/exposure/$exposure/${contestID.$oid}/${playerID.$oid}")
  }

  def getChart(contestID: BSONObjectID, playerName: String, chartName: String) = {
    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/${contestID.$oid}/$playerName")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Chat
  /////////////////////////////////////////////////////////////////////////////

  def sendChatMessage(contestId: BSONObjectID, message: Message) = {
    $http.put[js.Array[Message]](s"/api/contest/${contestId.$oid}/chat", message)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def deleteOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID) = {
    $http.delete[Contest](s"/api/order/${contestId.$oid}/${playerId.$oid}/$orderId")
  }

  def getHeldSecurities(playerId: BSONObjectID) = {
    $http.get[js.Array[String]](s"/api/positions/${playerId.$oid}")
  }

}

/**
  * Margin Market Value
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MarginMarketValue extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var name: String = js.native
  var marginMarketValue: Double = js.native
}
