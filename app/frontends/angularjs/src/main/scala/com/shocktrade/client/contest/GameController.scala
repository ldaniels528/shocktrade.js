package com.shocktrade.client.contest

import com.shocktrade.client.{GlobalLoading, MySessionService, RootScope}
import com.shocktrade.common.models.contest.ContestSearchResult
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Game Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
abstract class GameController($scope: GameScope,
                              $location: Location,
                              toaster: Toaster,
                              mySession: MySessionService,
                              portfolioService: PortfolioService)
  extends Controller with GlobalLoading {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.enterGame = (aContest: js.UndefOr[ContestSearchResult]) => aContest foreach enterGame

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def enterGame(contest: ContestSearchResult): Unit = {
    val results = (for {
      contestID <- contest.contestID
      userID <- $scope.userProfile.flatMap(_.userID)
    } yield (contestID, userID)).toOption

    results match {
      case Some((contestID, userID)) =>
        asyncLoading($scope)(portfolioService.findPortfolio(contestID, userID)) onComplete {
          case Success(response) =>
            val portfolio = response.data
            console.info(s"portfolio = ${JSON.stringify(portfolio)}")
            $location.path(s"/dashboard/$userID")
          case Failure(e) =>
            toaster.error(e.displayMessage)
        }
    }
  }

}

/**
 * Game Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameScope extends RootScope {
  // functions
  var enterGame: js.Function1[js.UndefOr[ContestSearchResult], Unit] = js.native

}
