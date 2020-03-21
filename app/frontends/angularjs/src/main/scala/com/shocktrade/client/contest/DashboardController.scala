package com.shocktrade.client.contest

import com.shocktrade.client.dialogs.PerksDialog
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.{ContestFactory, GlobalNavigation, RootScope}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import com.shocktrade.client.ScopeEvents._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Dashboard Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class DashboardController($scope: DashboardScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestFactory") contestFactory: ContestFactory,
                          @injected("ContestService") contestService: ContestService,
                          @injected("PerksDialog") perksDialog: PerksDialog,
                          @injected("PortfolioService") portfolioService: PortfolioService,
                          @injected("GameStateFactory") gameState: GameStateFactory)
  extends Controller {

  private var accountMode = false

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    for (contestID <- $routeParams.contestID) reload(contestID)
  }

  def reload(contestID: String): Unit = {
    contestFactory.findContest(contestID) onComplete {
      case Success(contest) =>
        if(contest.contestID != gameState.contest.flatMap(_.contestID)) $scope.$apply { () => gameState.contest = contest }
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Account Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isCashAccount = () => !accountMode

  $scope.isMarginAccount = () => accountMode

  $scope.toggleAccountMode = () => accountMode = !accountMode

  $scope.getAccountMode = () => accountMode

  $scope.getAccountType = () => "CASH"

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  $scope.popupPerksDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String]) => {
    console.info(s"popupPerksDialog: aContestID = ${(aContestID ?? $routeParams.contestID ?? gameState.contest.flatMap(_.contestID)).orNull}, aUserID = ${aUserID.orNull}")
    for {
      contestID <- aContestID ?? $routeParams.contestID ?? gameState.contest.flatMap(_.contestID)
      userID <- aUserID
    } {
      perksDialog.popup(contestID, userID) onComplete {
        case Success(_) => reload(contestID)
        case Failure(e) =>
          if (e.getMessage != "cancel") {
            e.printStackTrace()
          }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Participant Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isRankingsShown = () => !$scope.rankingsHidden.isTrue

  $scope.toggleRankingsShown = () => $scope.rankingsHidden = !$scope.rankingsHidden.isTrue

  ///////////////////////////////////////////////////////////////////////////
  //          Events
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserProfileUpdated { (_, profile) =>
    for {contestID <- $routeParams.contestID} reload(contestID)
  }

}

/**
 * Dashboard Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait DashboardScope extends RootScope with GlobalNavigation {
  // functions
  var init: js.Function0[Unit] = js.native
  var isCashAccount: js.Function0[Boolean] = js.native
  var isMarginAccount: js.Function0[Boolean] = js.native
  var isRankingsShown: js.Function0[Boolean] = js.native
  var getAccountMode: js.Function0[Boolean] = js.native
  var getAccountType: js.Function0[String] = js.native
  var popupPerksDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
  var toggleAccountMode: js.Function0[Unit] = js.native
  var toggleRankingsShown: js.Function0[Unit] = js.native

  // variables
  //var contest: js.UndefOr[Contest] = js.native
  var rankingsHidden: js.UndefOr[Boolean] = js.native

}

/**
 * Dashboard Route Params
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait DashboardRouteParams extends js.Object {
  var contestID: js.UndefOr[String] = js.native

}
