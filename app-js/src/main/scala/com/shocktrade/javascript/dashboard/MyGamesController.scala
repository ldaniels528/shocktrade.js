package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.{Location, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Scope, injected, scoped}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySessionService
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialog
import com.shocktrade.javascript.models.{BSONObjectID, Contest, ParticipantRanking, UserProfile}
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success}

/**
 * My Games Controller
 * @author lawrence.daniels@gmail.com
 */
class MyGamesController($scope: Scope, $location: Location, $timeout: Timeout, toaster: Toaster,
                        @injected("ContestService") contestService: ContestService,
                        @injected("MySessionService") mySession: MySessionService,
                        @injected("NewGameDialog") newGameDialog: NewGameDialog)
  extends GameController($scope, $location, toaster, mySession) {

  private var myContests = js.Array[Contest]()

  ///////////////////////////////////////////////////////////////////////////
  //          Scope Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def initMyGames() = reload()

  @scoped def getMyContests = myContests

  @scoped
  def getMyRankings(contest: Contest): UndefOr[ParticipantRanking] = {
    if (!isDefined(contest)) null
    else if (!isDefined(contest.rankings)) {
      (for {
        userId <- mySession.userProfile._id.toOption
        player <- contestService.getPlayerRankings(contest, userId).flatMap(_.player).toOption
      } yield player).orUndefined
    }
    else contest.rankings.flatMap(_.player)
  }

  @scoped
  def popupNewGameDialog() {
    newGameDialog.popup() onComplete {
      case Success(contest) =>
        if (contest.error.nonEmpty) toaster.error(contest.error)
        else {
          // TODO add to Contests
          reload()
        }

      case Failure(e) =>
        toaster.error("Failed to create game")
        g.console.error(s"Failed to create game ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def reload(): Unit = mySession.userProfile._id foreach loadMyContests

  private def loadMyContests(userID: BSONObjectID) {
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
  $scope.$on(ContestCreated, (event: js.Dynamic, contest: Contest) => reload())

  /**
   * Listen for contest deletion events
   */
  $scope.$on(ContestDeleted, (event: js.Dynamic, contest: Contest) => reload())

  /**
   * Listen for user profile changes
   */
  $scope.$on(UserProfileChanged, (event: js.Dynamic, profile: UserProfile) => reload())

}
