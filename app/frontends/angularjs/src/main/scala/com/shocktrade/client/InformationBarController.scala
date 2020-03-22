package com.shocktrade.client

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.users.GameStateFactory.{NetWorthScope, UserProfileScope}
import com.shocktrade.client.users.{GameStateFactory, UserService}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.{Controller, Q, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Information Bar Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class InformationBarController($scope: InformationBarControllerScope, $q: Q,
                               @injected("GameStateFactory") gameState: GameStateFactory,
                               @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                               @injected("UserService") userService: UserService,
                               @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller {

  implicit private val scope: InformationBarControllerScope = $scope

  // initialize public variables
  $scope.notifications = js.Array()

  //////////////////////////////////////////////////////////////////////
  //              Event Listeners
  //////////////////////////////////////////////////////////////////////

  $scope.onUserProfileUpdated { (_, profile) =>
    profile.userID foreach { userID =>
      console.info(s"Retrieving Net-worth for user $userID...")
      userService.getNetWorth(userID) onComplete {
        case Success(netWorth) => gameState.netWorth = netWorth.data
        case Failure(e) =>
          console.error(s"Failed retrieving Net-worth for user $userID: ${e.getMessage}")
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => console.log(s"${getClass.getSimpleName} is initializing...")

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
      netWorth <- gameState.userProfile
      cash <- netWorth.wallet
      funds <- netWorth.funds
      equity <- netWorth.equity
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
trait InformationBarControllerScope extends RootScope with NetWorthScope with UserProfileScope {
  // functions
  var init: js.Function0[Unit] = js.native
  var autoCompleteSearch: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[js.Array[EntitySearchResult]]]] = js.native
  var formatSearchResult: js.Function1[js.UndefOr[EntitySearchResult], js.UndefOr[String]] = js.native
  var getWealthChange: js.Function0[js.UndefOr[Double]] = js.native
  var hasNotifications: js.Function0[Boolean] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[js.Any], js.UndefOr[EntitySearchResult], js.UndefOr[String], Unit] = js.native

  // variables
  var notifications: js.Array[String] = js.native

}