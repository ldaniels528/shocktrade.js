package com.shocktrade.javascript

import ScalaJsHelper._
import com.ldaniels528.scalascript.core.{Http, Timeout}
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.{Controller, Scope, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.NavigationController._
import com.shocktrade.javascript.dashboard.ContestService
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Player Information Bar Controller
 * @author lawrence.daniels@gmail.com
 */
class NavigationController($scope: js.Dynamic, $http: Http, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("MySession") mySession: MySession,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

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
    mySession.userProfile.OID_? match {
      case Some(userID) =>
        console.log(s"Loading player information for user ID $userID")

        // load the player's total investment
        loadTotalInvestment(userID)
        attemptsLeft = 3

      case None =>
        attemptsLeft -= 1
        if (attemptsLeft > 0) {
          console.log("No user ID found... awaiting re-try (5 seconds)")
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
    console.log("Loading Total investment...")
    contestService.getTotalInvestment(playerId) onComplete {
      case Success(response) =>
        totalInvestment = response.netWorth.asOpt[Double]
        totalInvestmentStatus = Option(LOADED)
        console.log("Total investment loaded")
      case Failure(e) =>
        toaster.error("Error loading total investment")
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