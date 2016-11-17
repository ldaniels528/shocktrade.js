package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.{PerksDialog, TransferFundsDialog}
import com.shocktrade.client.{GlobalNavigation, MySessionService}
import com.shocktrade.common.models.contest.Participant
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope, Timeout, injected}
import org.scalajs.dom.browser.console
import org.scalajs.sjs.JsUnderOrHelper._

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Dashboard Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class DashboardController($scope: DashboardScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestService") contestService: ContestService,
                          @injected("MySessionService") mySession: MySessionService,
                          @injected("PerksDialog") perksDialog: PerksDialog,
                          @injected("PortfolioService") portfolioService: PortfolioService,
                          @injected("TransferFundsDialog") transferFundsDialog: TransferFundsDialog)
  extends Controller {

  private var accountMode = false

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    if (!mySession.isAuthenticated || mySession.portfolio_?.isEmpty) $scope.switchToDiscover()
  }

  /////////////////////////////////////////////////////////////////////
  //          Account Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isCashAccount = () => !accountMode

  $scope.isMarginAccount = () => accountMode

  $scope.toggleAccountMode = () => accountMode = !accountMode

  $scope.getAccountMode = () => accountMode

  $scope.getAccountType = () => if (accountMode) "MARGIN" else "CASH"

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  $scope.popupPerksDialog = () => {
    perksDialog.popup() onComplete {
      case Success(portfolio) =>
        $scope.$apply(() => mySession.updatePortfolio(portfolio))
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          e.printStackTrace()
        }
    }
  }

  $scope.popupTransferFundsDialog = () => {
    transferFundsDialog.popup() onComplete {
      case Success(portfolio) => mySession.updatePortfolio(portfolio)
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          e.printStackTrace()
        }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Participant Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isRankingsShown = () => !mySession.contest_?.exists(_.rankingsHidden.isTrue)

  $scope.toggleRankingsShown = () => mySession.contest_?.foreach(c => c.rankingsHidden = !c.rankingsHidden.isTrue)

  $scope.getRankings = () => {
    mySession.contest_?.orUndefined flatMap { contest =>
      mySession.updateRankings(contest).participants
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // if a contest ID was passed, load the contest.
  $routeParams.contestId foreach loadContest

  private def loadContest(contestId: String) {
    console.info(s"Attempting to load contest $contestId...")
    mySession.userProfile._id.toOption match {
      case Some(playerId) =>
        mySession.loadContestByID(contestId) onComplete {
          case Success(_) => $scope.$apply(() => {})
          case Failure(e) =>
            toaster.error(s"Error loading contest")
            console.error(s"Error loading contest $contestId: ${e.displayMessage}")
            e.printStackTrace()
        }
      case None =>
        console.warn(s"Contest #$contestId cannot be loaded because the player ID is missing")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Events
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserProfileChanged((_, profile) => $routeParams.contestId foreach loadContest)

}

/**
  * Dashboard Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DashboardScope extends Scope with GlobalNavigation {
  // functions
  var init: js.Function0[Unit] = js.native
  var getRankings: js.Function0[js.UndefOr[js.Array[Participant]]] = js.native
  var isCashAccount: js.Function0[Boolean] = js.native
  var isMarginAccount: js.Function0[Boolean] = js.native
  var isRankingsShown: js.Function0[Boolean] = js.native
  var getAccountMode: js.Function0[Boolean] = js.native
  var getAccountType: js.Function0[String] = js.native
  var popupPerksDialog: js.Function0[Unit] = js.native
  var popupTransferFundsDialog: js.Function0[Unit] = js.native
  var toggleAccountMode: js.Function0[Unit] = js.native
  var toggleRankingsShown: js.Function0[Unit] = js.native

}

/**
  * Dashboard Route Params
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DashboardRouteParams extends js.Object {
  var contestId: js.UndefOr[String] = js.native

}
