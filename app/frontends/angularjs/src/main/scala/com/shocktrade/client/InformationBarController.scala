package com.shocktrade.client

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.UserService
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.{Controller, Q, Scope, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Information Bar Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class InformationBarController($scope: InformationBarControllerScope, $cookies: Cookies, $q: Q,
                               @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                               @injected("UserService") userService: UserService,
                               @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  implicit private val cookies: Cookies = $cookies

  // initialize public variables
  $scope.notifications = js.Array()
  $scope.userProfile = js.undefined

  //////////////////////////////////////////////////////////////////////
  //              Initialization Functions
  //////////////////////////////////////////////////////////////////////

  $scope.initInfoBar = () => {
    console.log(s"${getClass.getSimpleName} is initializing...")
    $cookies.getGameState.userID.foreach(initInfoBar)
  }

  $scope.onUserProfileUpdated { (_, profile) => $scope.userProfile = profile}

  private def initInfoBar(userID: String): Unit ={
    userService.findUserByID(userID) onComplete {
      case Success(userProfile) => $scope.$apply(() => $scope.userProfile = userProfile.data)
      case Failure(e) => console.error(e.getMessage)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.autoCompleteSearch = (aSearchTerm: js.UndefOr[String]) => aSearchTerm.map(autoCompleteSearch)

  $scope.formatSearchResult = (aResult: js.UndefOr[EntitySearchResult]) => aResult.flatMap(_.name)

  $scope.getWealthChange = () => getWealthChange

  $scope.hasNotifications = () => $scope.notifications.nonEmpty

  $scope.isWebSocketConnected = () => webSocket.isConnected

  $scope.onSelectedItem = (item: js.UndefOr[js.Any], aModel: js.UndefOr[EntitySearchResult], aLabel: js.UndefOr[String]) => {
    onSelectedItem(item, aModel, aLabel)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def autoCompleteSearch(searchTerm: String): js.Promise[js.Array[EntitySearchResult]] = {
    val deferred = $q.defer[js.Array[EntitySearchResult]]()
    reactiveSearchSvc.search(searchTerm, maxResults = 20) onComplete {
      case Success(response) => deferred.resolve(response.data)
      case Failure(e) => deferred.reject(e.displayMessage)
    }
    deferred.promise
  }

  private def getWealthChange: js.UndefOr[Double] = {
    val original = 250e+3
    for {
      userProfile <- $scope.userProfile
      cash <- userProfile.wallet
      funds <- userProfile.funds
      equity <- userProfile.equity
      change = ((cash + funds + equity) - original) / original * 100.0
    } yield change
  }

  private def onSelectedItem(item: js.UndefOr[js.Any], aModel: js.UndefOr[EntitySearchResult], label: js.UndefOr[String]): Unit = {
    console.info(s"item = ${JSON.stringify(item.orNull)}; aModel = ${JSON.stringify(aModel.orNull)}; label = ${label.orNull}")
    for {
      model <- aModel
      entity <- model.`type`
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
 * Information Bar Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait InformationBarControllerScope extends Scope {
  // functions
  var initInfoBar: js.Function0[Unit] = js.native
  var autoCompleteSearch: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[js.Array[EntitySearchResult]]]] = js.native
  var formatSearchResult: js.Function1[js.UndefOr[EntitySearchResult], js.UndefOr[String]] = js.native
  var getWealthChange: js.Function0[js.UndefOr[Double]] = js.native
  var hasNotifications: js.Function0[Boolean] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[js.Any], js.UndefOr[EntitySearchResult], js.UndefOr[String], Unit] = js.native

  // variables
  var notifications: js.Array[String] = js.native
  var userProfile: js.UndefOr[UserProfile] = js.native

}