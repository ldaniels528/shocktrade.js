package com.shocktrade.javascript.dashboard

import biz.enef.angulate.core.{Location, Timeout}
import biz.enef.angulate.named
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialogService

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Failure, Success}

/**
 * My Games Controller
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class MyGamesController($scope: js.Dynamic, $location: Location, $timeout: Timeout, toaster: Toaster,
                        @named("ContestService") contestService: ContestService,
                        @named("MySession") mySession: MySession,
                        @named("NewGameDialogService") newGameDialog: NewGameDialogService)
  extends GameController($scope, $location, toaster, mySession) {

  private var myContests = js.Array[js.Dynamic]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => mySession.userProfile.OID_? foreach loadMyContests

  $scope.enterGame = (contest: js.Dynamic) => enterGame(contest)

  $scope.getMyContests = () => myContests

  $scope.getMyRankings = (contest: js.Dynamic) => {
    if (!isDefined(contest)) null
    else if (!isDefined(contest.ranking)) {
      val rankings = contestService.getPlayerRankings(contest, mySession.getUserID())
      rankings.player
    }
    else contest.ranking.player
  }

  $scope.newGamePopup = () => {
    newGameDialog.popup() onComplete {
      case Success(contest) =>
        myContests.push(contest.asInstanceOf[js.Dynamic])
      case Failure(e) =>
        toaster.error("Failed to create game")
        g.console.error(s"Failed to create game ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def loadMyContests(userID: String) {
    if (mySession.isAuthenticated()) {
      g.console.log(s"Loading 'My Contests' for user '$userID'...")
      contestService.getContestsByPlayerID(userID) onComplete {
        case Success(contests) =>
          g.console.log(s"Loaded ${contests.length} contest(s)")
          myContests = contests
        case Failure(e) =>
          toaster.error("Failed to load 'My Contests'", null)
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.$on("contest_created", (event: js.Dynamic, contest: js.Dynamic) => loadMyContests(mySession.userProfile.OID))

  /**
   * Listen for contest deletion events
   */
  $scope.$on("contest_deleted", (event: js.Dynamic, contest: js.Dynamic) => loadMyContests(mySession.userProfile.OID))

  /**
   * Listen for user profile changes
   */
  $scope.$watch(mySession.getUserID, { (newUserID: String, oldUserID: String) =>
    g.console.log(s"newUserID = $newUserID, oldUserID = $oldUserID")
    if (newUserID != null) loadMyContests(newUserID)
  })

}
