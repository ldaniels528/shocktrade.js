package com.shocktrade.stockguru.contest

import com.shocktrade.common.models.contest.{ContestRankings, PortfolioRanking}
import com.shocktrade.stockguru.MySessionService
import com.shocktrade.stockguru.ScopeEvents._
import com.shocktrade.stockguru.dialogs.{PerksDialog, TransferFundsDialog}
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
      case Success(contest) => mySession.setContest(contest)
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
      // if the rankings are not loaded, load them
      if (contest.rankings.isEmpty) {
        mySession.userProfile._id foreach { playerId =>
          contest.rankings = new ContestRankings()
          mySession.getContestRankings(contest, playerId) onSuccess { case rankings =>
            $scope.$apply(() => contest.rankings = rankings)
          }
        }
      }

      contest.rankings.flatMap(_.participants)
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
trait DashboardScope extends Scope {
  // functions
  var isCashAccount: js.Function0[Boolean] = js.native
  var isMarginAccount: js.Function0[Boolean] = js.native
  var toggleAccountMode: js.Function0[Unit] = js.native
  var getAccountMode: js.Function0[Boolean] = js.native
  var getAccountType: js.Function0[String] = js.native
  var popupPerksDialog: js.Function0[Unit] = js.native
  var popupTransferFundsDialog: js.Function0[Unit] = js.native
  var isRankingsShown: js.Function0[Boolean] = js.native
  var toggleRankingsShown: js.Function0[Unit] = js.native
  var getRankings: js.Function0[js.UndefOr[js.Array[PortfolioRanking]]] = js.native

}

/**
  * Dashboard Route Params
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DashboardRouteParams extends js.Object {
  var contestId: js.UndefOr[String] = js.native

}
