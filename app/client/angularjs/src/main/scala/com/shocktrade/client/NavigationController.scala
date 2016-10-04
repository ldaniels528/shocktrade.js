package com.shocktrade.client

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ContestService, PortfolioService}
import com.shocktrade.common.models.contest.PortfolioRanking
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope, Timeout, injected, _}
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Navigation Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NavigationController($scope: NavigationControllerScope, $http: Http, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("MySessionService") mySession: MySessionService,
                           @injected("PortfolioService") portfolioService: PortfolioService,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  private val LOADING = "LOADING"
  private val LOADED = "LOADED"
  private val FAILED = "FAILED"
  private val TIMEOUT = "TIMEOUT"

  private var totalInvestmentStatus: Option[String] = None
  private var totalInvestment: Option[Double] = None
  private var attemptsLeft = 3
  private var isVisible = true

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initNav = () => retrieveTotalInvestment()

  $scope.isAuthenticated = () => mySession.isAuthenticated

  $scope.isBarVisible = () => isVisible

  $scope.getMyRanking = () => {
    (for {
      contest <- mySession.contest_?
      playerID <- mySession.userProfile._id.toOption
      rankings <- portfolioService.getPlayerRankings(contest, playerID).flatMap(_.player).toOption
    } yield rankings).orUndefined
  }

  $scope.getTotalInvestment = () => totalInvestment getOrElse 0.00d

  $scope.getTotalInvestmentStatus = () => totalInvestmentStatus getOrElse LOADING

  $scope.isTotalInvestmentLoaded = () => totalInvestment.isDefined

  $scope.reloadTotalInvestment = () => totalInvestmentStatus = None

  $scope.toggleVisibility = () => isVisible = !isVisible

  $scope.isWebSocketConnected = () => webSocket.isConnected

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def retrieveTotalInvestment() {
    mySession.userProfile._id.toOption match {
      case Some(userID) =>
        console.log(s"Loading player information for user ID $userID")

        // load the player's total investment
        loadTotalInvestment(userID)
        attemptsLeft = 3

      case None =>
        attemptsLeft -= 1
        if (attemptsLeft > 0) {
          console.log("No user ID found... awaiting re-try (5 seconds)")
          $timeout(() => retrieveTotalInvestment(), 5.seconds)
        }
    }
  }

  private def loadTotalInvestment(playerId: String) = {
    // set a timeout so that loading doesn't persist
    $timeout(() =>
      if (totalInvestment.isEmpty) {
        console.error("Total investment call timed out")
        totalInvestmentStatus = Option(TIMEOUT)
      }, 20.seconds)

    // retrieve the total investment
    console.log("Loading Total investment...")
    portfolioService.getTotalInvestment(playerId) onComplete {
      case Success(response) =>
        totalInvestment = response.investment.toOption
        totalInvestmentStatus = Option(LOADED)
        console.log("Total investment loaded")
      case Failure(e) =>
        toaster.error("Error loading total investment")
        totalInvestmentStatus = Option(FAILED)
        console.error("Total investment call failed")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Listen for changes to the player's profile
    */
  $scope.onUserProfileChanged((_, profile) => retrieveTotalInvestment())

}

/**
  * Navigation Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NavigationControllerScope extends Scope {
  var initNav: js.Function0[Unit] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native
  var isBarVisible: js.Function0[Boolean] = js.native
  var isTotalInvestmentLoaded: js.Function0[Boolean] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native

  var getMyRanking: js.Function0[js.UndefOr[PortfolioRanking]] = js.native
  var getTotalInvestment: js.Function0[Double] = js.native
  var getTotalInvestmentStatus: js.Function0[String] = js.native

  var reloadTotalInvestment: js.Function0[Unit] = js.native
  var toggleVisibility: js.Function0[Unit] = js.native
}
