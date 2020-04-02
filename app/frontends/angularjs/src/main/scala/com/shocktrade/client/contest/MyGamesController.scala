package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.NewGameDialog
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.models.contest.MyContest
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * My Games Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class MyGamesController($rootScope: RootScope, $scope: MyGamesScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                             @injected("ContestService") contestService: ContestService,
                             @injected("NewGameDialog") newGameDialog: NewGameDialog,
                             @injected("UserService") userService: UserService)
  extends Controller with ContestEntrySupport[MyGamesScope] with GlobalLoading {

  private var myContests = js.Array[MyContest]()

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => initMyGames()

  /**
   * Listen for contest creation events
   */
  $scope.onContestCreated((_, _) => initMyGames())

  /**
   * Listen for contest deletion events
   */
  $scope.onContestDeleted((_, _) => initMyGames())

  /**
   * Listen for contest selected events
   */
  $scope.onContestSelected((_, _) => initMyGames())

  /**
   * Listen for user profile changes
   */
  $scope.onUserProfileUpdated((_, _) => initMyGames())

  /**
   * Retrieves the collection of games for the authenticated user
   */
  private def initMyGames(): Unit = $scope.userProfile.flatMap(_.userID) foreach loadMyContests

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getMyContests = () => myContests

  $scope.popupNewGameDialog = (aUserID: js.UndefOr[String]) => aUserID.foreach(popupNewGameDialog)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def popupNewGameDialog(userID: String): Unit = {
    val outcome = for {
      response <- newGameDialog.popup(userID)
      userProfile <- userService.findUserByID(userID)
    } yield (response, userProfile)

    outcome onComplete {
      case Success((_, userProfile)) =>
        initMyGames()
        $rootScope.emitUserProfileUpdated(userProfile.data)
      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game: ${e.displayMessage}")
    }
  }

  private def loadMyContests(userID: String): Unit = {
    console.log(s"Loading 'My Contests' for user '$userID'...")
    contestService.findMyContests(userID) onComplete {
      case Success(response) =>
        val contests = response.data
        console.log(s"Loaded ${contests.length} contest(s)")
        $scope.$apply(() => myContests = contests)
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${JSON.stringify(e.displayMessage)}")
    }
  }

}

/**
 * My Games Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait MyGamesScope extends GameSearchScope {
  // functions
  var initMyGames: js.Function0[Unit] = js.native
  var getMyContests: js.Function0[js.Array[MyContest]] = js.native
  var popupNewGameDialog: js.Function1[js.UndefOr[String], Unit] = js.native

}
