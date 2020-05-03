package com.shocktrade.client

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ContestEntrySupport, ContestEntrySupportScope}
import com.shocktrade.client.dialogs.StockQuoteDialog.{StockQuoteDialogSupport, StockQuoteDialogSupportScope}
import com.shocktrade.client.dialogs.{PlayerProfileDialog, StockQuoteDialog}
import com.shocktrade.client.users.UserService
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Information Bar Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class InformationBarController($scope: InformationBarControllerScope, $cookies: Cookies,
                                    $location: Location, $q: Q, toaster: Toaster,
                                    @injected("GameStateService") gameStateService: GameStateService,
                                    @injected("PlayerProfileDialog") playerProfileDialog: PlayerProfileDialog,
                                    @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                                    @injected("StockQuoteDialog") stockQuoteDialog: StockQuoteDialog,
                                    @injected("UserService") userService: UserService,
                                    @injected("WebSocketService") webSocket: WebSocketService)
  extends Controller with ContestEntrySupport with PlayerProfilePopupSupport with StockQuoteDialogSupport {

  implicit private val cookies: Cookies = $cookies

  // initialize public variables
  $scope.notifications = js.Array()
  $scope.userProfile = js.undefined

  //////////////////////////////////////////////////////////////////////
  //              Initialization Functions
  //////////////////////////////////////////////////////////////////////

  $scope.initInfoBar = () => {
    console.log(s"${getClass.getSimpleName} is initializing...")
    gameStateService.getUserID.map(initInfoBar)
  }

  $scope.onUserProfileUpdated { (_, profile) => $scope.userProfile = profile }

  private def initInfoBar(userID: String): js.Promise[HttpResponse[UserProfile]] = {
    val outcome = userService.findUserByID(userID)
    outcome onComplete {
      case Success(userProfile) => $scope.$apply(() => $scope.userProfile = userProfile.data)
      case Failure(e) => console.error(e.getMessage)
    }
    outcome
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
      equity <- userProfile.equity
      change = ((cash + equity) - original) / original * 100.0
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
        case "SIMULATION" => model._id.foreach(enterGame)
        case "STOCK" => model._id.foreach(stockQuoteDialog.lookupSymbol)
        case "USER" => model._id.foreach(playerProfileDialog.popup)
        case _ =>
          console.warn(s"Entity type '$entity' was unhandled")
      }
    }
  }

  def showSymbol(symbol: String): Location = $location.path(s"/discover?symbol=$symbol")

}

/**
 * Information Bar Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait InformationBarControllerScope extends Scope
  with ContestEntrySupportScope with PlayerProfilePopupSupportScope with StockQuoteDialogSupportScope {
  // functions
  var initInfoBar: js.Function0[js.UndefOr[js.Promise[HttpResponse[UserProfile]]]] = js.native
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