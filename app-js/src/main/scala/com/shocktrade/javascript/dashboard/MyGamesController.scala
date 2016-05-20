package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Location, Timeout}
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.angularjs.injected
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dialogs.NewGameDialog
import com.shocktrade.javascript.models.{BSONObjectID, Contest, ParticipantRanking, UserProfile}
import org.scalajs.dom.{Event, console}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * My Games Controller
  * @author lawrence.daniels@gmail.com
  */
class MyGamesController($scope: MyGamesScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                        @injected("ContestService") contestService: ContestService,
                        @injected("MySessionService") mySession: MySessionService,
                        @injected("NewGameDialog") newGameDialog: NewGameDialog)
  extends GameController($scope, $location, toaster, mySession) {

  private var myContests = js.Array[Contest]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => reload()

  $scope.getMyContests = () => myContests

  $scope.getMyRankings = (aContest: js.UndefOr[Contest]) => aContest flatMap { contest =>
    contest.rankings.map(_.player) getOrElse {
      for {
        userId <- mySession.userProfile._id
        player <- contestService.getPlayerRankings(contest, userId).flatMap(_.player)
      } yield player
    }
  }

  $scope.popupNewGameDialog = () => {
    newGameDialog.popup() onComplete {
      case Success(contest) =>
        if (contest.error.nonEmpty) toaster.error(contest.error)
        else {
          // TODO add to Contests
          reload()
        }

      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def reload(): Unit = mySession.userProfile._id foreach loadMyContests

  private def loadMyContests(userID: BSONObjectID) {
    console.log(s"Loading 'My Contests' for user '${userID.$oid}'...")
    contestService.getContestsByPlayerID(userID) onComplete {
      case Success(contests) =>
        console.log(s"Loaded ${contests.length} contest(s)")
        myContests = contests
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Listen for contest creation events
    */
  $scope.$on(ContestCreated, (event: Event, contest: Contest) => reload())

  /**
    * Listen for contest deletion events
    */
  $scope.$on(ContestDeleted, (event: Event, contest: Contest) => reload())

  /**
    * Listen for user profile changes
    */
  $scope.$on(UserProfileChanged, (event: Event, profile: UserProfile) => reload())

}

/**
  * My Games Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MyGamesScope extends GameScope {

  // functions
  var initMyGames: js.Function0[Unit]
  var getMyContests: js.Function0[js.Array[Contest]]
  var getMyRankings: js.Function1[js.UndefOr[Contest], js.UndefOr[ParticipantRanking]]
  var popupNewGameDialog: js.Function0[Unit]

}
