package com.shocktrade.javascript.dashboard

import biz.enef.angulate.core.Timeout
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.TransferFundsDialog

import scala.concurrent.ExecutionContext.Implicits.global
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
                          @named("TransferFundsDialog") transferFundsDialog: TransferFundsDialog)
  extends ScopeController {

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

  $scope.marginAccountDialog = () => {
    transferFundsDialog.popup().onComplete {
      case Success(contest: js.Dynamic) => mySession.setContest(contest)
      case Success(response) =>
        g.console.info(s"The process succeeded, but got back the wrong object - $response")
      case Failure(e) =>
        toaster.error("Error transferring funds")
    }
  }

  $scope.perksDialog = () => perksDialog.popup(JS())

  /////////////////////////////////////////////////////////////////////
  //          Participant Functions
  /////////////////////////////////////////////////////////////////////

  $scope.isRankingsShown = () => !mySession.contest.exists(_.rankingsHidden.as[Boolean])

  $scope.toggleRankingsShown = () => mySession.contest.foreach(c => c.rankingsHidden = !c.rankingsHidden)

  $scope.getRankings = () => mySession.contest match {
    case Some(c) =>
      val rankings = contestService.getPlayerRankings(c, mySession.getUserID())
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
      contestService.getContestByID(contestId) onComplete {
        case Success(loadedContest) => mySession.setContest(loadedContest)
        case Failure(e) =>
          g.console.error(s"Error loading contest $contestId")
          toaster.error("Error loading game", null)
          e.printStackTrace()
      }
    }
  }

}