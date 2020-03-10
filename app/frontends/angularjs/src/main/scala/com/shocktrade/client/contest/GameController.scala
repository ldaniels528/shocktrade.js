package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{ContestSearchResultUI, Portfolio}
import com.shocktrade.client.{MySessionService, RootScope}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location}

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

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.enterGame = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest foreach enterGame

  $scope.isParticipant = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest exists  isParticipant

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def enterGame(contest: ContestSearchResultUI): Unit = {
    val results = for {
      contestID <- contest.contestID.toOption
      portfolioID <- mySession.userProfile.userID.toOption
    } yield (contestID, portfolioID)

    results match {
      case Some((contestID, portfolioID)) =>
        //$scope.$apply(() => contest.loading = true)
        portfolioService.findParticipant(contestID, portfolioID).toFuture onComplete {
          case Success(response) =>
            val portfolio = response.data
            console.info(s"portfolio = ${JSON.stringify(portfolio)}")
            //$scope.$apply(() => contest.loading = false)
            $location.path(s"/dashboard/$contestID")
          case Failure(e) =>
            contest.loading = false
        }
    }
  }

  private def isParticipant(contest: ContestSearchResultUI): Boolean = $scope.userProfile.exists(_.userID == contest.hostUserID)

}

/**
 * Game Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameScope extends RootScope {
  // functions
  var enterGame: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var isParticipant: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native

  // variables
 var portfolio: js.UndefOr[Portfolio] = js.native

}
