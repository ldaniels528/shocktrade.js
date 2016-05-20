package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.meansjs.angularjs.Timeout
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Controller, Scope, injected}
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dialogs.{PerksDialog, TransferFundsDialog}
import com.shocktrade.javascript.models.{BSONObjectID, ParticipantRanking}
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Dashboard Controller
  * @author lawrence.daniels@gmail.com
  */
class DashboardController($scope: DashboardScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestService") contestService: ContestService,
                          @injected("MySessionService") mySession: MySessionService,
                          @injected("PerksDialog") perksDialog: PerksDialog,
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
      case Success(contest) =>
        mySession.setContest(contest)
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

  $scope.isRankingsShown = () => !mySession.contest.exists(_.rankingsHidden.exists(_ == true))

  $scope.toggleRankingsShown = () => mySession.contest.foreach(c => c.rankingsHidden = c.rankingsHidden.map(!_))

  $scope.getRankings = () => mySession.contest match {
    case Some(contest) =>
      (for {
        userId <- mySession.userProfile._id.toOption
        rankings <- contestService.getPlayerRankings(contest, userId).toOption
        participants = rankings.participants
      } yield participants) getOrElse emptyArray
    case None =>
      emptyArray
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // if a contest ID was passed ...
  $routeParams.contestId foreach { contestId =>
    // if the current contest is not the chosen contest ...
    if (!mySession.contest.exists(c => BSONObjectID.isEqual(c._id, contestId))) {
      console.log(s"Loading contest $contestId...")
      contestService.getContestByID(contestId) onComplete {
        case Success(loadedContest) => mySession.setContest(loadedContest)
        case Failure(e) =>
          console.error(s"Error loading contest $contestId")
          toaster.error("Error loading game")
          e.printStackTrace()
      }
    }
  }

}

/**
  * Dashboard Scope
  * @author lawrence.daniels@gmail.com
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
  var getRankings: js.Function0[js.Array[_ <: ParticipantRanking]] = js.native

}

/**
  * Dashboard Route Params
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait DashboardRouteParams extends js.Object {
  var contestId: js.UndefOr[BSONObjectID] = js.native

}
