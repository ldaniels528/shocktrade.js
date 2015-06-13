package com.shocktrade.javascript.dashboard

import biz.enef.angulate.core.Timeout
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Dashboard Controller
 * @author lawrence.daniels@gmail.com
 */
class DashboardController($scope: js.Dynamic, $routeParams: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                          @named("ContestService") contestService: ContestService,
                          @named("MySession") mySession: MySession,
                          @named("PerksDialog") perksDialog: js.Dynamic,
                          @named("TransferFundsDialog") transferFundsDialog: js.Dynamic) extends ScopeController {

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

  $scope.marginAccountDialog = () => transferFundsDialog.popup(JS(success = (contest: js.Dynamic) => mySession.setContest_@(contest)))

  $scope.perksDialog = () => perksDialog.popup(JS())

  /////////////////////////////////////////////////////////////////////
  //          Participant Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isRankingsShown = () => !mySession.contest.exists(_.rankingsHidden.as[Boolean])

  $scope.toggleRankingsShown = () => mySession.contest.foreach(c => c.rankingsHidden = !c.rankingsHidden)

  $scope.getRankings = () => mySession.contest match {
    case Some(c) =>
      val rankings = contestService.getPlayerRankings_@(c, mySession.getUserID_@)
      rankings.participants
    case None => emptyArray[js.Dynamic]
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // if a contest ID was passed ...
  if (isDefined($routeParams.contestId)) {
    val contestId = $routeParams.contestId.as[String]

    // if the current contest is not the chosen contest ...
    if (!mySession.contest.exists(_.OID == contestId)) {
      g.console.log(s"Loading contest $contestId...")
      contestService.getContestByID_@(contestId) onComplete {
        case Success(loadedContest) => mySession.setContest_@(loadedContest)
        case Failure(e) =>
          g.console.error(s"Error loading contest $contestId")
          toaster.pop("error", "Error loading game", null)
          e.printStackTrace()
      }
    }
  }

}