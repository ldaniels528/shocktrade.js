package com.shocktrade.javascript.admin

import biz.enef.angulate.core.HttpService
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Inspect Controller
 * @author lawrence.daniels@gmail.com
 */
class InspectController($scope: js.Dynamic, $http: HttpService, $routeParams: js.Dynamic, toaster: Toaster,
                        @named("ContestService") contestService: ContestService,
                        @named("MySession") mySession: MySession) extends ScopeController {

  $scope.contest = null

  $scope.expandItem = (item: js.Dynamic) => item.expanded = !item.expanded

  $scope.expandPlayer = (player: js.Dynamic) => {
    player.expanded = !player.expanded
    if (!isDefined(player.myOpenOrders)) player.myOpenOrders = JS()
    if (!isDefined(player.myClosedOrders)) player.myClosedOrders = JS()
    if (!isDefined(player.myPositions)) player.myPositions = JS()
    if (!isDefined(player.myPerformance)) player.myPerformance = JS()
  }

  $scope.getOpenOrders = (contest: js.Dynamic) => {
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

  $scope.updateContestHost = (host: js.Dynamic) => {
    $http.post[js.Dynamic](s"/api/contest/${$scope.contest.OID}/host", JS(host = host)) onComplete {
      case Success(response) =>
        toaster.pop("success", "Processing host updated", null)
      case Failure(e) =>
        toaster.pop("error", "Failed to update processing host", null)
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
        toaster.pop("error", "Failed to load contest " + contestId, null)
    }
  }

}
