package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Http, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.github.ldaniels528.scalascript.{Controller, Scope, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.NavigationController._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.models.{BSONObjectID, ParticipantRanking, UserProfile}
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Navigation Controller
  * @author lawrence.daniels@gmail.com
  */
class NavigationController($scope: NavigationControllerScope, $http: Http, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("MySessionService") mySession: MySessionService,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  private var totalInvestmentStatus: Option[String] = None
  private var totalInvestment: Option[Double] = None
  private var attemptsLeft = 3
  private var isVisible = false

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initNav = () => retrieveTotalInvestment()

  $scope.isAuthenticated = () => mySession.isAuthenticated

  $scope.isBarVisible = () => isVisible

  $scope.getMyRanking = () => {
    (for {
      contest <- mySession.contest
      playerID <- mySession.userProfile._id.toOption
      rankings <- contestService.getPlayerRankings(contest, playerID).flatMap(_.player).toOption
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

  private def loadTotalInvestment(playerId: BSONObjectID) = {
    // set a timeout so that loading doesn't persist
    $timeout(() =>
      if (totalInvestment.isEmpty) {
        console.error("Total investment call timed out")
        totalInvestmentStatus = Option(TIMEOUT)
      }, 20.seconds)

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
        console.error("Total investment call failed")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Listen for changes to the player's profile
    */
  $scope.$on(UserProfileChanged, (profile: UserProfile) => retrieveTotalInvestment())

}

/**
  * Navigation Controller Scope
  */
@js.native
trait NavigationControllerScope extends Scope {
  var initNav: js.Function0[Unit]
  var isAuthenticated: js.Function0[Boolean]
  var isBarVisible: js.Function0[Boolean]
  var isTotalInvestmentLoaded: js.Function0[Boolean]
  var isWebSocketConnected: js.Function0[Boolean]

  var getMyRanking: js.Function0[js.UndefOr[ParticipantRanking]]
  var getTotalInvestment: js.Function0[Double]
  var getTotalInvestmentStatus: js.Function0[String]

  var reloadTotalInvestment: js.Function0[Unit]
  var toggleVisibility: js.Function0[Unit]
}

/**
  * Player Information Bar Controller Singleton
  */
object NavigationController {
  val LOADING = "LOADING"
  val LOADED = "LOADED"
  val FAILED = "FAILED"
  val TIMEOUT = "TIMEOUT"

}