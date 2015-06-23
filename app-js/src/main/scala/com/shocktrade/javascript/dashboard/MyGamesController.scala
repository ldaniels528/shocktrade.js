package com.shocktrade.javascript.dashboard

import com.shocktrade.javascript.AppEvents._
import biz.enef.angulate.core.{Location, Timeout}
import biz.enef.angulate.{Scope, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialogService

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * My Games Controller
 * @author lawrence.daniels@gmail.com
 */
class MyGamesController($scope: js.Dynamic, $location: Location, $timeout: Timeout, toaster: Toaster,
                        @named("ContestService") contestService: ContestService,
                        @named("MySession") mySession: MySession,
                        @named("NewGameDialogService") newGameDialog: NewGameDialogService)
  extends GameController($scope, $location, toaster, mySession) {

  private var myContests = js.Array[js.Dynamic]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => init()

  $scope.enterGame = (contest: js.Dynamic) => enterGame(contest)

  $scope.getMyContests = () => myContests

  $scope.getMyRankings = (contest: js.Dynamic) => getMyRankings(contest)

  $scope.newGamePopup = () => newGamePopup()

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def init(): Unit = mySession.userProfile.OID_? foreach loadMyContests

  private def getMyRankings(contest: js.Dynamic) = {
    if (!isDefined(contest)) null
    else if (!isDefined(contest.ranking)) {
      val rankings = contestService.getPlayerRankings(contest, mySession.getUserID())
      rankings.player
    }
    else contest.ranking.player
  }

  private def loadMyContests(userID: String) {
    g.console.log(s"Loading 'My Contests' for user '$userID'...")
    contestService.getContestsByPlayerID(userID) onComplete {
      case Success(contests) =>
        g.console.log(s"Loaded ${contests.length} contest(s)")
        myContests = contests
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        g.console.error(s"Failed to load 'My Contests': ${e.getMessage}")
    }
  }

  private def newGamePopup() = {
    newGameDialog.popup() onComplete {
      case Success(contest) =>
        myContests.push(contest.asInstanceOf[js.Dynamic])
      case Failure(e) =>
        toaster.error("Failed to create game")
        g.console.error(s"Failed to create game ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  private val scope = $scope.asInstanceOf[Scope]

  /**
   * Listen for contest creation events
   */
  scope.$on(ContestCreated, (event: js.Dynamic, contest: js.Dynamic) => init())

  /**
   * Listen for contest deletion events
   */
  scope.$on(ContestDeleted, (event: js.Dynamic, contest: js.Dynamic) => init())

  /**
   * Listen for user profile changes
   */
  scope.$on(UserProfileChanged, (event: js.Dynamic, profile: js.Dynamic) => loadMyContests(profile.OID))

}
