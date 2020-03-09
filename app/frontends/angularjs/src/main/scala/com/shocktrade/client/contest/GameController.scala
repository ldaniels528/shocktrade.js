package com.shocktrade.client.contest

import com.shocktrade.client.MySessionService
import com.shocktrade.client.models.contest.ContestSearchResultUI
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location, Scope}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Abstract Game Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
abstract class GameController($scope: GameScope,
                              $location: Location,
                              toaster: Toaster,
                              mySession: MySessionService,
                              portfolioService: PortfolioService)
  extends Controller {

  $scope.enterGame = (aContest: js.UndefOr[ContestSearchResultUI]) => enterGame(aContest)

  $scope.isParticipant = (aContest: js.UndefOr[ContestSearchResultUI]) => isParticipant(aContest)

  private def enterGame(aContest: js.UndefOr[ContestSearchResultUI]): Unit = aContest foreach { contest =>
    if ($scope.isParticipant(contest)) {
      val results = for {
        contestID <- contest.contestID.toOption
        portfolioID <- mySession.userProfile.userID.toOption
      } yield (contestID, portfolioID)

      results match {
        case Some((contestID, portfolioID)) =>
          contest.loading = true
          mySession.loadContestByID(contestID) onComplete {
            case Success(result) =>
              $scope.$apply(() => contest.loading = false)
              $location.path(s"/dashboard/$contestID")
            case Failure(e) =>
              contest.loading = false
          }
        case None =>
          toaster.error("The contest has not been loaded properly")
      }
    }
    else {
      toaster.error("You must join the contest first")
    }
  }

  private def isParticipant(aContest: js.UndefOr[ContestSearchResultUI]): Boolean = aContest exists { contest =>
    contest.hostUserID == mySession.userProfile.userID
  }

}

/**
 * Game Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameScope extends Scope {
  // functions
  var enterGame: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var isParticipant: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native

}
