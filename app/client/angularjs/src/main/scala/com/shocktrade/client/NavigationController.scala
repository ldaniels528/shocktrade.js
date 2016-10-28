package com.shocktrade.client

import com.shocktrade.common.models.contest.Participant
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.{Controller, Q, Scope, injected}
import org.scalajs.dom.browser.console
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Navigation Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NavigationController($scope: NavigationControllerScope, $q: Q,
                           @injected("MySessionService") mySession: MySessionService,
                           @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                           @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.autoCompleteSearch = (searchTerm: String) => {
    val deferred = $q.defer[js.Array[EntitySearchResult]]()
    reactiveSearchSvc.search(searchTerm, maxResults = 20) onComplete {
      case Success(response) => deferred.resolve(response)
      case Failure(e) => deferred.reject(e.displayMessage)
    }
    deferred.promise
  }

  $scope.formatSearchResult = (aResult: js.UndefOr[EntitySearchResult]) => for {
    result <- aResult
    name <- result.name
  } yield name

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

  $scope.isWebSocketConnected = () => webSocket.isConnected

  $scope.onSelectedItem = (item: js.UndefOr[js.Any], aModel: js.UndefOr[EntitySearchResult], label: js.UndefOr[String]) => {
    for {
      model <- aModel
      entity <- model.`type`
      modelId <- model._id
    } {
      console.log(s"Handling $entity $label")
      entity match {
        case _ =>
          console.warn(s"Entity type '$entity' was unhandled")
      }
    }
  }

}

/**
  * Navigation Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NavigationControllerScope extends Scope {
  var autoCompleteSearch: js.Function1[String, js.Promise[js.Array[EntitySearchResult]]] = js.native
  var formatSearchResult: js.Function1[js.UndefOr[EntitySearchResult], js.UndefOr[String]] = js.native
  var getMyRanking: js.Function0[js.UndefOr[Participant]] = js.native
  var getRankings: js.Function0[js.UndefOr[js.Array[Participant]]] = js.native
  var getWealthChange: js.Function0[Double] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[js.Any], js.UndefOr[EntitySearchResult], js.UndefOr[String], Unit] = js.native

}
