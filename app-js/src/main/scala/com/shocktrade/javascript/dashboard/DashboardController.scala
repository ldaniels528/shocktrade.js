package com.shocktrade.javascript.dashboard

import com.greencatsoft.angularjs.{AbstractController, _}
import com.greencatsoft.angularjs.core.Scope
import com.shocktrade.javascript.dashboard.DashboardController.DashboardScope

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Dashboard Controller
 * @author lawrence.daniels@gmail.com
 */
@JSExport
@injectable("DashboardController")
class DashboardController(scope: DashboardScope, contestService: ContestService)
  extends AbstractController[DashboardScope](scope) {

  @JSExport
  def isCashAccount = !scope.accountMode

  @JSExport
  def isMarginAccount = scope.accountMode

  @JSExport
  def toggleAccountMode() {
    scope.accountMode = !scope.accountMode
  }

  @JSExport
  def getAccountMode = scope.accountMode

  @JSExport
  def getAccountType = if (isMarginAccount) "MARGIN" else "CASH"

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  /*
  def marginAccountDialog() {
    TransferFundsDialog.popup(js.Dictionary(
      "success": function (contest) {
        MySession.setContest(contest)
      })
    })
  }

  def perksDialog() = PerksDialog.popup()
*/
  /////////////////////////////////////////////////////////////////////
  //          Participant Functions
  /////////////////////////////////////////////////////////////////////

  @JSExport
  def isRankingsShown = true // !MySession.getContest().rankingsHidden

  @JSExport
  def toggleRankingsShown() {
    /*
    val contest = MySession.getContest()
    contest.rankingsHidden = !contest.rankingsHidden*/
  }

  @JSExport
  def getRankings = {
    /*
    if (MySession.contestIsEmpty) js.Array()
    else {
      val rankings = ContestService.getPlayerRankings(MySession.getContest(), MySession.getUserName())
       rankings.participants
    }*/
    js.Array()
  }

}

object DashboardController {

  /**
   * Dashboard Scope
   * @author lawrence.daniels@gmail.com
   */
  trait DashboardScope extends Scope {

    var accountMode: Boolean = js.native

  }

}