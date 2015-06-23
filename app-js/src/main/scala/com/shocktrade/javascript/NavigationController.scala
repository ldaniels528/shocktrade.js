package com.shocktrade.javascript

import biz.enef.angulate.core.{HttpService, Timeout}
import biz.enef.angulate.{Scope, ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.NavigationController._
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Player Information Bar Controller
 * @author lawrence.daniels@gmail.com
 */
class NavigationController($scope: js.Dynamic, $http: HttpService, $timeout: Timeout, toaster: Toaster,
                           @named("ContestService") contestService: ContestService,
                           @named("MySession") mySession: MySession,
                           @named("WebSocketService") webSocket: WebSocketService)
  extends ScopeController {

  private val scope = $scope.asInstanceOf[Scope]
  private var totalInvestmentStatus: Option[String] = None
  private var totalInvestment: Option[Double] = None
  private var attemptsLeft = 3
  private var isVisible = false

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initNav = () => init()

  $scope.isAuthenticated = () => mySession.isAuthenticated()

  $scope.getMyRanking = () => getMyRanking getOrElse JS()

  $scope.getTotalInvestment = () => getTotalInvestment

  $scope.getTotalInvestmentStatus = totalInvestmentStatus getOrElse LOADING

  $scope.isTotalInvestmentLoaded = () => isTotalInvestmentLoaded

  $scope.reloadTotalInvestment = () => reloadTotalInvestment()

  $scope.isBarVisible = () => isVisible

  $scope.toggleVisibility = () => isVisible = !isVisible

  $scope.webSockectConnected = () => webSocket.isConnected()

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def init() {
    g.console.log(s"NavigationController init is running...")
    mySession.userProfile.OID_? match {
      case Some(userID) =>
        g.console.log(s"Loading player information for user ID $userID")

        // load the player's total investment
        loadTotalInvestment(userID)
        attemptsLeft = 3

      case None =>
        attemptsLeft -= 1
        if (attemptsLeft > 0) {
          g.console.log("No user ID found... awaiting re-try (5 seconds)")
          $timeout(() => init(), 5000)
        }
    }
  }

  private def getMyRanking: Option[js.Dynamic] = {
    for {
      contest <- mySession.contest
      playerID <- mySession.userProfile.OID_?
    } yield {
      contestService.getPlayerRankings(contest, playerID).player
    }
  }

  private def getTotalInvestment = totalInvestment getOrElse 0.00d

  private def isTotalInvestmentLoaded = totalInvestment.isDefined

  private def reloadTotalInvestment() = totalInvestmentStatus = None

  private def loadTotalInvestment(playerId: String) = {
    // set a timeout so that loading doesn't persist
    $timeout({ () =>
      if (totalInvestment.isEmpty) {
        g.console.error("Total investment call timed out")
        totalInvestmentStatus = Option(TIMEOUT)
      }
    }, delay = 20000)

    // retrieve the total investment
    g.console.log("Loading Total investment...")
    contestService.getTotalInvestment(playerId) onComplete {
      case Success(response) =>
        totalInvestment = Option(response.netWorth.as[Double])
        totalInvestmentStatus = Option(LOADED)
        g.console.log("Total investment loaded")
      case Failure(e) =>
        toaster.error("Error loading total investment", null)
        totalInvestmentStatus = Option(FAILED)
        g.console.error("Total investment call failed")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for changes to the player's profile
   */
  scope.$on(UserProfileChanged, (profile: js.Dynamic) => init())

}

/**
 * Player Information Bar Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object NavigationController {

  val LOADING = "LOADING"
  val LOADED = "LOADED"
  val FAILED = "FAILED"
  val TIMEOUT = "TIMEOUT"

}