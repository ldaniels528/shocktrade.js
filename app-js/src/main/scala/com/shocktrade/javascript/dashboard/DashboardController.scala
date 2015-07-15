package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.Timeout
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.{PerksDialog, TransferFundsDialog}
import com.shocktrade.javascript.models.ParticipantRanking
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * Dashboard Controller
 * @author lawrence.daniels@gmail.com
 */
class DashboardController($scope: DashboardScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestService") contestService: ContestService,
                          @injected("MySession") mySession: MySession,
                          @injected("PerksDialog") perksDialog: PerksDialog,
                          @injected("TransferFundsDialog") transferFundsDialog: TransferFundsDialog)
  extends Controller {

  private var accountMode = false

  /////////////////////////////////////////////////////////////////////
  //          Account Functions
  /////////////////////////////////////////////////////////////////////

  @scoped def isCashAccount = !accountMode

  @scoped def isMarginAccount = accountMode

  @scoped def toggleAccountMode() = accountMode = !accountMode

  @scoped def getAccountMode = accountMode

  @scoped def getAccountType = if (accountMode) "MARGIN" else "CASH"

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  @scoped def popupPerksDialog() = {
    perksDialog.popup() onComplete {
      case Success(contest) =>
        mySession.setContest(contest)
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          e.printStackTrace()
        }
    }
  }

  @scoped def popupTransferFundsDialog() = {
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

  @scoped def isRankingsShown = !mySession.contest.exists(_.rankingsHidden.exists(_ == true))

  @scoped def toggleRankingsShown() = mySession.contest.foreach(c => c.rankingsHidden = c.rankingsHidden.map(!_))

  @scoped
  def getRankings: js.Array[ParticipantRanking] = mySession.contest match {
    case Some(c) =>
      (for {
        userId <- mySession.userProfile.OID_?
        rankings <- contestService.getPlayerRankings(c, userId).toOption
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
    if (!mySession.contest.exists(_.OID_?.contains(contestId))) {
      console.log(s"Loading contest $contestId...")
      contestService.getContestByID(contestId) onComplete {
        case Success(loadedContest) => mySession.setContest(loadedContest)
        case Failure(e) =>
          g.console.error(s"Error loading contest $contestId")
          toaster.error("Error loading game")
          e.printStackTrace()
      }
    }
  }

}

/**
 * Dashboard Controller Scope
 * @author lawrence.daniels@gmail.com
 */
trait DashboardScope extends Scope {

}

/**
 * Dashboard Route Params
 * @author lawrence.daniels@gmail.com
 */
trait DashboardRouteParams extends js.Object {
  var contestId: js.UndefOr[String] = js.native

}
