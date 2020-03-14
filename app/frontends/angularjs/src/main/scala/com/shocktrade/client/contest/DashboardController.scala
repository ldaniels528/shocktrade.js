package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.PerksDialog
import com.shocktrade.client.{GlobalNavigation, RootScope}
import com.shocktrade.common.models.contest.Participant
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Dashboard Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class DashboardController($scope: DashboardScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestService") contestService: ContestService,
                          @injected("PerksDialog") perksDialog: PerksDialog,
                          @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller {

  private var accountMode = false

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    //if (!mySession.isAuthenticated || mySession.portfolio_?.isEmpty) $scope.switchToDiscover()
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
    console.info(s"aContestID = ${(aContestID ?? $routeParams.contestID ?? $scope.contest.flatMap(_.contestID)).orNull}, aUserID = ${aUserID.orNull}")
    for {
      contestID <- aContestID ?? $routeParams.contestID ?? $scope.contest.flatMap(_.contestID)
      userID <- aUserID
    } {
      perksDialog.popup(contestID, userID) onComplete {
        case Success(portfolio) =>
          $scope.$apply(() => $scope.portfolio = portfolio)
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

  $scope.getRankings = () => {
    /*
    $scope.contest.orUndefined flatMap { contest =>
      mySession.updateRankings(contest).participants
    }*/
    js.undefined
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // if a contest ID was passed, load the contest.
  console.info(s"PARAMS: contest '${$routeParams.contestID.orNull}' user '${JSON.stringify($scope.userProfile.orNull)}' networth: ${JSON.stringify($scope.netWorth.orNull)}'")
  for {
    contestID <- $routeParams.contestID
    userID <- $scope.userProfile.flatMap(_.userID)
  } loadContest(contestID, userID)

  def loadContest(contestID: String, userID: String): Unit = {
    console.info(s"Loading portfolio for contest '$contestID' user '$userID'...'")
    portfolioService.findPortfolio(contestID, userID) onComplete {
      case Success(response) => $scope.$apply { () => $scope.portfolio = response.data }
      case Failure(e) =>
        toaster.error(s"Error loading contest")
        console.error(s"Error loading contest $contestID: ${e.displayMessage}")
        e.printStackTrace()
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Events
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserProfileChanged { (_, profile) =>
    console.info(s"UPDATE: contest '${$routeParams.contestID.orNull}' user '${JSON.stringify($scope.userProfile.orNull)}' networth: ${JSON.stringify($scope.netWorth.orNull)}'")
    for {
      contestID <- $routeParams.contestID
      userID <- profile.userID
    } loadContest(contestID, userID)
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
  var getRankings: js.Function0[js.UndefOr[js.Array[Participant]]] = js.native
  var isCashAccount: js.Function0[Boolean] = js.native
  var isMarginAccount: js.Function0[Boolean] = js.native
  var isRankingsShown: js.Function0[Boolean] = js.native
  var getAccountMode: js.Function0[Boolean] = js.native
  var getAccountType: js.Function0[String] = js.native
  var popupPerksDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
  var toggleAccountMode: js.Function0[Unit] = js.native
  var toggleRankingsShown: js.Function0[Unit] = js.native

  // variables
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
