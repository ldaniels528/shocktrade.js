package com.shocktrade.client

import com.shocktrade.common.models.contest.Participant
import org.scalajs.angularjs.{Controller, Scope, injected}
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Navigation Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NavigationController($scope: NavigationControllerScope,
                           @injected("MySessionService") mySession: MySessionService,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  private var isVisible = true

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getMyRanking = () => {
    for {
      contest <- mySession.contest_?.orUndefined
      player <- mySession.updateRankings(contest).player
    } yield player
  }

  $scope.getRankings = () => {
    mySession.contest_?.orUndefined flatMap { contest =>
      mySession.updateRankings(contest).participants
    }
  }

  $scope.getWealthChange = () => mySession.userProfile.netWorth.map(nw => 100 * (nw - 250e+3) / 250e+3).orZero

  $scope.isAuthenticated = () => mySession.isAuthenticated

  $scope.isBarVisible = () => isVisible

  $scope.isWebSocketConnected = () => webSocket.isConnected

  $scope.toggleVisibility = () => isVisible = !isVisible

}

/**
  * Navigation Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NavigationControllerScope extends Scope {
  var getMyRanking: js.Function0[js.UndefOr[Participant]] = js.native
  var getRankings: js.Function0[js.UndefOr[js.Array[Participant]]] = js.native
  var getWealthChange: js.Function0[Double] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native
  var isBarVisible: js.Function0[Boolean] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native
  var toggleVisibility: js.Function0[Unit] = js.native
}
