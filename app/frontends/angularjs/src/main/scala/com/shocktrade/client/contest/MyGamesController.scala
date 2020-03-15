package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.NewGameDialog
import com.shocktrade.client.{ContestFactory, GlobalLoading}
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
case class MyGamesController($scope: MyGamesScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                             @injected("ContestFactory") contestFactory: ContestFactory,
                             @injected("ContestService") contestService: ContestService,
                             @injected("NewGameDialog") newGameDialog: NewGameDialog)
  extends Controller with ContestEntrySupport[MyGamesScope] with GlobalLoading {

  private var myContests = js.Array[MyContest]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => reload()

  $scope.getMyContests = () => myContests

  $scope.popupNewGameDialog = () => popupNewGameDialog()

  $scope.rankOf = (rank: js.UndefOr[Int]) => rank.map(rankOf)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def popupNewGameDialog(): Unit = {
    newGameDialog.popup() onComplete {
      case Success(_) => $scope.$apply(() => reload())
      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game: ${e.displayMessage}")
    }
  }

  private def reload(): Unit = $scope.userProfile.flatMap(_.userID) foreach loadMyContests

  private def loadMyContests(userID: String): Unit = {
    console.log(s"Loading 'My Contests' for user '$userID'...")
    contestService.findMyContests(userID) onComplete {
      case Success(response) if response.status == 200 =>
        val contests = response.data
        console.log(s"Loaded ${contests.length} contest(s)")
        $scope.$apply(() => myContests = contests)
      case Success(response) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${response.statusText}")
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${JSON.stringify(e.displayMessage)}")
    }
  }

  private def rankOf(rank: Int): String = {
    val suffix = rank.toString match {
      case n if n.endsWith("11") | n.endsWith("12") | n.endsWith("13") => "th"
      case n if n.endsWith("1") => "st"
      case n if n.endsWith("2") => "nd"
      case n if n.endsWith("3") => "rd"
      case _ => "th"
    }
    s"$rank$suffix"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.onContestCreated((_, contest) => reload())

  /**
   * Listen for contest deletion events
   */
  $scope.onContestDeleted((_, contest) => reload())

  /**
   * Listen for user profile changes
   */
  $scope.onUserProfileChanged((_, profile) => reload())

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
  var popupNewGameDialog: js.Function0[Unit] = js.native
  var rankOf: js.Function1[js.UndefOr[Int], js.UndefOr[String]] = js.native

}
