package com.shocktrade.javascript.admin

import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Controller, Scope, injected}
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.models._
import org.scalajs.dom.console

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Inspect Controller
  * @author lawrence.daniels@gmail.com
  */
class InspectController($scope: InspectControllerScope, $http: Http, $routeParams: InspectRouteParams, toaster: Toaster,
                        @injected("ContestService") contestService: ContestService,
                        @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  $scope.contest = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandItem = (anItem: js.UndefOr[ExpandableItem]) => anItem foreach { item =>
    item.expanded = !item.expanded.contains(true)
  }

  $scope.expandPlayer = (aPlayer: js.UndefOr[ExpandablePlayer]) => aPlayer foreach { player =>
    player.expanded = !player.expanded.contains(true)
    if (!isDefined(player.myOpenOrders)) player.myOpenOrders = emptyArray[Order]
    if (!isDefined(player.myClosedOrders)) player.myClosedOrders = emptyArray[ClosedOrder]
    if (!isDefined(player.myPositions)) player.myPositions = emptyArray[Position]
    if (!isDefined(player.myPerformance)) player.myPerformance = emptyArray[Performance]
  }

  $scope.getOpenOrders = (aContest: js.UndefOr[Contest]) => {
    aContest map (_.participants.flatMap(_.orders))
  }

  $scope.updateContestHost = (aHost: js.UndefOr[String]) => {
    for {
      contestId <- $scope.contest.flatMap(_._id)
      host <- aHost
    } {
      contestService.updateContestHost(contestId, host) onComplete {
        case Success(response) =>
          toaster.success("Processing host updated")
        case Failure(e) =>
          toaster.error("Failed to update processing host")
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $routeParams.contestId foreach { contestId =>
    console.log(s"Attempting to load contest $contestId")

    // load the contest
    contestService.getContestByID(BSONObjectID(contestId)) onComplete {
      case Success(contest) =>
        $scope.contest = contest
        mySession.setContest(contest)
      case Failure(e) =>
        toaster.error("Failed to load contest " + contestId)
    }
  }

}

/**
  * Inspect Scope Controller
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait InspectControllerScope extends Scope {
  // variables
  var contest: js.UndefOr[Contest] = js.native

  // functions
  var expandItem: js.Function1[js.UndefOr[ExpandableItem], Unit] = js.native
  var expandPlayer: js.Function1[js.UndefOr[ExpandablePlayer], Unit] = js.native
  var getOpenOrders: js.Function1[js.UndefOr[Contest], js.UndefOr[js.Array[Order]]] = js.native
  var updateContestHost: js.Function1[js.UndefOr[String], Unit] = js.native

}

/**
  * Inspect Route Params
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait InspectRouteParams extends js.Object {
  var contestId: js.UndefOr[String] = js.native

}

/**
  * Expandable Item
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExpandableItem extends js.Object {
  var expanded: js.UndefOr[Boolean] = js.native

}

/**
  * Expandable Player
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExpandablePlayer extends js.Object {
  var expanded: js.UndefOr[Boolean] = js.native
  var myOpenOrders: js.UndefOr[js.Array[Order]] = js.native
  var myClosedOrders: js.UndefOr[js.Array[ClosedOrder]] = js.native
  var myPositions: js.UndefOr[js.Array[Position]] = js.native
  var myPerformance: js.UndefOr[js.Array[Performance]] = js.native
}
