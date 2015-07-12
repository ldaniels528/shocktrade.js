package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.{Location, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{scoped, Scope, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialogService
import com.shocktrade.javascript.models.Contest
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * My Games Controller
 * @author lawrence.daniels@gmail.com
 */
class MyGamesController($scope: Scope, $location: Location, $timeout: Timeout, toaster: Toaster,
                        @injected("ContestService") contestService: ContestService,
                        @injected("MySession") mySession: MySession,
                        @injected("NewGameDialogService") newGameDialog: NewGameDialogService)
  extends GameController($scope, $location, toaster, mySession) {

  private var myContests = js.Array[Contest]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def initMyGames() = init()

  @scoped def getMyContests = myContests

  @scoped
  def getMyRankings(contest: Contest) = {
    if (!isDefined(contest)) null
    else if (!isDefined(contest.rankings)) {
      val rankings = contestService.getPlayerRankings(contest, mySession.getUserID)
      rankings.player
    }
    else contest.rankings.player
  }

  @scoped
  def newGamePopup() {
    newGameDialog.popup() onComplete {
      case Success(contest) =>
        if (isDefined(contest.dynamic.error)) toaster.error(contest.dynamic.error)
        else {
          // TODO add to Contests
          init()
        }

      case Failure(e) =>
        toaster.error("Failed to create game")
        g.console.error(s"Failed to create game ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def init(): Unit = mySession.userProfile.OID_? foreach loadMyContests

  private def loadMyContests(userID: String) {
    console.log(s"Loading 'My Contests' for user '$userID'...")
    contestService.getContestsByPlayerID(userID) onComplete {
      case Success(contests) =>
        console.log(s"Loaded ${contests.length} contest(s)")
        myContests = contests
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        g.console.error(s"Failed to load 'My Contests': ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.$on(ContestCreated, (event: js.Dynamic, contest: Contest) => init())

  /**
   * Listen for contest deletion events
   */
  $scope.$on(ContestDeleted, (event: js.Dynamic, contest: Contest) => init())

  /**
   * Listen for user profile changes
   */
  $scope.$on(UserProfileChanged, (event: js.Dynamic, profile: js.Dynamic) => init())

}
