package com.shocktrade.javascript.admin

import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.javascript.angularjs.core.Http
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Inspect Controller
 * @author lawrence.daniels@gmail.com
 */
class InspectController($scope: js.Dynamic, $http: Http, $routeParams: js.Dynamic, toaster: Toaster,
                        @named("ContestService") contestService: ContestService,
                        @named("MySession") mySession: MySession) extends ScopeController {

  /////////////////////////////////////////////////////////////////////
  //          Public Variables
  /////////////////////////////////////////////////////////////////////

  $scope.contest = null

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandItem = (item: js.Dynamic) => item.expanded = !item.expanded

  $scope.expandPlayer = (player: js.Dynamic) => expandPlayer(player)

  $scope.getOpenOrders = (contest: js.Dynamic) => getOpenOrders(contest)

  $scope.updateContestHost = (host: js.Dynamic) => updateContestHost(host)

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def expandPlayer(player: js.Dynamic) = {
    player.expanded = !player.expanded
    if (!isDefined(player.myOpenOrders)) player.myOpenOrders = JS()
    if (!isDefined(player.myClosedOrders)) player.myClosedOrders = JS()
    if (!isDefined(player.myPositions)) player.myPositions = JS()
    if (!isDefined(player.myPerformance)) player.myPerformance = JS()
  }

  private def getOpenOrders(contest: js.Dynamic) = {
    val orders = emptyArray[js.Dynamic]
    if (isDefined(contest)) {
      contest.participants.asArray[js.Dynamic] foreach { participant =>
        participant.orders.asArray[js.Dynamic] foreach { order =>
          order.owner = participant
          orders.push(order)
        }
      }
    }
    orders
  }

  private def updateContestHost(host: js.Dynamic) = {
    $http.post[js.Dynamic](s"/api/contest/${$scope.contest.OID}/host", JS(host = host)) onComplete {
      case Success(response) =>
        toaster.success("Processing host updated")
      case Failure(e) =>
        toaster.error("Failed to update processing host")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  if (isDefined($routeParams.contestId)) {
    val contestId = $routeParams.contestId.as[String]
    g.console.log(s"Attempting to load contest $contestId")

    // load the contest
    contestService.getContestByID(contestId) onComplete {
      case Success(contest) =>
        $scope.contest = contest
        mySession.setContest(contest)
      case Failure(e) =>
        toaster.error("Failed to load contest " + contestId)
    }
  }

}
