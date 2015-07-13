package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Http, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.NavigationController._
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.models.{ParticipantRanking, UserProfile}
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success}

/**
 * Player Information Bar Controller
 * @author lawrence.daniels@gmail.com
 */
class NavigationController($scope: Scope, $http: Http, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("MySession") mySession: MySession,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  private var totalInvestmentStatus: Option[String] = None
  private var totalInvestment: Option[Double] = None
  private var attemptsLeft = 3
  private var isVisible = false

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def initNav() = retrieveTotalInvestment()

  @scoped def isAuthenticated = mySession.isAuthenticated

  @scoped def isBarVisible = isVisible

  @scoped def getMyRanking: UndefOr[ParticipantRanking] = {
    (for {
      contest <- mySession.contest
      playerID <- mySession.userProfile.OID_?
      rankings <- contestService.getPlayerRankings(contest, playerID).flatMap(_.player).toOption
    } yield rankings).orUndefined
  }

  @scoped def getTotalInvestment = totalInvestment getOrElse 0.00d

  @scoped def getTotalInvestmentStatus = totalInvestmentStatus getOrElse LOADING

  @scoped def isTotalInvestmentLoaded = totalInvestment.isDefined

  @scoped def reloadTotalInvestment() = totalInvestmentStatus = None

  @scoped def toggleVisibility() = isVisible = !isVisible

  @scoped def isWebSocketConnected = webSocket.isConnected

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def retrieveTotalInvestment() {
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
          $timeout(() => retrieveTotalInvestment(), 5.seconds)
        }
    }
  }

  private def loadTotalInvestment(playerId: String) = {
    // set a timeout so that loading doesn't persist
    $timeout(() =>
      if (totalInvestment.isEmpty) {
        g.console.error("Total investment call timed out")
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
        g.console.error("Total investment call failed")
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
 * Player Information Bar Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object NavigationController {

  val LOADING = "LOADING"
  val LOADED = "LOADED"
  val FAILED = "FAILED"
  val TIMEOUT = "TIMEOUT"

}