package com.shocktrade.client

import com.shocktrade.client.MainController._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.GameLevel
import com.shocktrade.client.dialogs.SignUpDialog
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.GameStateFactory.NetWorthScope
import com.shocktrade.client.users.{AuthenticationService, GameStateFactory, SignInDialog, UserService}
import com.shocktrade.common.models.quote.ClassifiedQuote
import com.shocktrade.common.models.user.OnlineStatus
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster._
import io.scalajs.npm.angularjs.uibootstrap.Modal
import io.scalajs.npm.angularjs.{Controller, Location, Timeout, injected}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Main Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class MainController($scope: MainControllerScope, $http: Http, $location: Location, $timeout: Timeout, toaster: Toaster, $uibModal: Modal,
                     @injected("AuthenticationService") authenticationService: AuthenticationService,
                     @injected("ContestFactory") contestFactory: ContestFactory,
                     @injected("GameStateFactory") gameState: GameStateFactory,
                     @injected("SignInDialog") signInDialog: SignInDialog,
                     @injected("SignUpDialog") signUpDialog: SignUpDialog,
                     @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading {

  implicit private val scope: MainControllerScope = $scope
  private val onlinePlayers = js.Dictionary[OnlineStatus]()
  private var loadingIndex = 0

  // public variable
  $scope.appTabs = MainTab.Tabs
  $scope.levels = GameLevel.Levels
  $scope.favoriteSymbols = js.Dictionary()
  $scope.recentSymbols = js.Dictionary()

  //////////////////////////////////////////////////////////////////////
  //              Event Listeners
  //////////////////////////////////////////////////////////////////////

  $scope.onUserProfileUpdated { (_, profile) =>
    console.log(s"profile = ${JSON.stringify(profile)}")
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Loading Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isLoading = () => loadingIndex > 0

  $scope.startLoading = (timeout: js.UndefOr[Int]) => {
    loadingIndex += 1
    val _timeout = timeout getOrElse DEFAULT_TIMEOUT

    // set loading timeout
    $timeout(() => {
      console.log(s"Disabling the loading animation due to time-out (${_timeout} msec)...")
      loadingIndex = 0
    }, _timeout)
  }

  $scope.stopLoading = (promise: js.UndefOr[js.Promise[js.Any]]) => {
    $timeout.cancel(promise)
    $timeout(() => if (loadingIndex > 0) loadingIndex -= 1, 500.millis)
    ()
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.mainInit = () => console.log(s"Initializing ${getClass.getSimpleName}...")

  $scope.getAssetCode = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetCode(q)

  $scope.getAssetIcon = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetIcon(q)

  $scope.getExchangeClass = (exchange: js.UndefOr[String]) => s"${normalizeExchange(exchange)} bold"

  $scope.getTabIndex = () => $scope.appTabs.indexWhere(tab => $location.path.contains(tab.url))

  $scope.isAuthenticated = () => $scope.userProfile.flatMap(_.userID).isAssigned

  $scope.normalizeExchange = (market: js.UndefOr[String]) => MainController.normalizeExchange(market)

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  $scope.isOnline = (aPlayer: js.UndefOr[UserProfile]) => aPlayer.flatMap(_.userID).exists(isOnline)

  $scope.getPreferenceIcon = (q: js.Dynamic) => getPreferenceIcon(q)

  $scope.logout = () => logout()

  $scope.signIn = () => signIn()

  $scope.signUp = () => signUp()

  private def clearLoggedInItems(): Unit = {
    contestFactory.clear()
    gameState.reset()
    $scope.favoriteSymbols.clear()
    $scope.recentSymbols.clear()
    $scope.userProfile = js.undefined
  }

  private def getPreferenceIcon(q: js.Dynamic): String = {
    // fail-safe
    if (!isDefined(q) || !isDefined(q.symbol)) ""
    else {
      // check for favorite and held securities
      q.symbol.asOpt[String] map { symbol =>
        if ($scope.favoriteSymbols.exists(_._1 == symbol)) "fa fa-heart"
        //else if (heldSecurities.isHeld(symbol)) "fa fa-star"
        else ""
      } getOrElse ""
    }
  }

  private def isOnline(userID: String): Boolean = { // TODO fix async drop-out
    if (!onlinePlayers.contains(userID)) {
      onlinePlayers(userID) = new OnlineStatus(connected = false)
      userService.getOnlineStatus(userID) onComplete {
        case Success(response) =>
          val newState = response.data
          onlinePlayers(userID) = newState
        case Failure(e) =>
          console.error(s"Error retrieving online state for user $userID: ${e.getMessage}")
      }
    }
    onlinePlayers.get(userID).exists(_.connected)
  }

  private def logout(): Unit = {
    authenticationService.logout() onComplete {
      case Success(_) => clearLoggedInItems()
      case Failure(e) =>
        toaster.error("An error occurred during logout")
        clearLoggedInItems()
        e.printStackTrace()
    }
  }

  private def signIn(): Unit = {
    val outcome = for {
      userAccount <- signInDialog.signIn()
      userProfile <- userAccount.userID.toOption match {
        case Some(userID) => userService.findUserByID(userID).toFuture
        case None => Future.failed(js.JavaScriptException("Missing user ID"))
      }
      netWorth <- userAccount.userID.toOption match {
        case Some(userID) => userService.getNetWorth(userID).toFuture
        case None => Future.failed(js.JavaScriptException("Missing user ID"))
      }
    } yield (userProfile, netWorth)

    outcome onComplete {
      case Success((userProfile, netWorth)) =>
        $scope.emitUserProfileUpdated(userProfile.data)
        $scope.$apply { () =>
          gameState.userProfile = userProfile.data
          gameState.netWorth = netWorth.data
        }
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  private def signUp(): Unit = {
    val outcome = for {
      userAccount <- signUpDialog.signUp()
      userProfile <- userAccount.userID.toOption match {
        case Some(userID) => userService.findUserByID(userID).toFuture
        case None => Future.failed(js.JavaScriptException("Missing user ID"))
      }
    } yield userProfile

    outcome onComplete {
      case Success(userProfile) => gameState.userProfile = userProfile.data
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Tab-related Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isVisibleTab = (aTab: js.UndefOr[MainTab]) => aTab.exists { tab =>
    (loadingIndex == 0) && (!tab.contestRequired && (!tab.authenticationRequired || $scope.userProfile.flatMap(_.userID).isAssigned))
  }

  $scope.switchToDiscover = () => $scope.switchToTab(MainTab.Discover)

  $scope.switchToGameSearch = () => $scope.switchToTab(MainTab.Search)

  $scope.switchToHome = () => $scope.switchToTab(MainTab.Home)

  $scope.switchToNewsFeed = () => $scope.switchToTab(MainTab.NewsFeed)

  $scope.switchToTab = (anIndex: js.UndefOr[Int]) => anIndex foreach switchToTab

  private def switchToTab(tabIndex: Int): Unit = {
    gameState.userProfile.flatMap(_.userID).toOption match {
      case Some(userID) =>
        asyncLoading($scope)(userService.setIsOnline(userID)) onComplete {
          case Success(response) =>
            console.info(s"response = ${JSON.stringify(response)}")
            performTabSwitch(tabIndex)
          case Failure(e) =>
            toaster.error(e.getMessage)
            performTabSwitch(tabIndex)
        }
      case None =>
        performTabSwitch(tabIndex)
    }
  }

  private def performTabSwitch(tabIndex: Int): Unit = {
    val tab = MainTab.Tabs(tabIndex)
    console.log(s"Changing location for ${$scope.userProfile.flatMap(_.username).orNull} to ${tab.url}")
    $location.url(tab.url)
  }

}

/**
 * Main Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object MainController {
  private val DEFAULT_TIMEOUT = 15000

  protected[client] def getAssetCode(q: js.UndefOr[ClassifiedQuote]): String = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "&#xf15a" // fa-bitcoin
      case "Currency" => "&#xf155" // fa-dollar
      case "ETF" => "&#xf18d" // fa-stack-exchange
      case _ => "&#xf0ac" // fa-globe
    } getOrElse ""
  }

  protected[client] def getAssetIcon(q: js.UndefOr[ClassifiedQuote]): String = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    } getOrElse "fa fa-globe st_blue"
  }

  protected[client] def normalizeExchange(aMarket: js.UndefOr[String]): String = {
    aMarket.flat map (_.toUpperCase) map {
      //case s if s.contains("ASE") => s
      //case s if s.contains("CCY") => s
      case s if s.contains("NAS") => "NASDAQ"
      case s if s.contains("NCM") => "NASDAQ"
      case s if s.contains("NGM") => "NASDAQ"
      case s if s.contains("NMS") => "NASDAQ"
      case s if s.contains("NYQ") => "NYSE"
      case s if s.contains("NYS") => "NYSE"
      case s if s.contains("OBB") => "OTCBB"
      case s if s.contains("OTC") => "OTCBB"
      case s if s.contains("OTHER") => "OTHER_OTC"
      //case s if s.contains("PCX") => s
      case s if s.contains("PNK") => "OTCBB"
      case s => s
    } getOrElse ""
  }

}

/**
 * Main Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait MainControllerScope extends RootScope with GlobalNavigation with NetWorthScope {
  // variables
  var appTabs: js.Array[MainTab] = js.native
  var favoriteSymbols: js.Dictionary[String] = js.native
  var levels: js.Array[GameLevel] = js.native
  //var netWorth: js.UndefOr[NetWorth] = js.native
  var recentSymbols: js.Dictionary[String] = js.native

  // loading functions
  var isLoading: js.Function0[Boolean]
  var startLoading: js.Function1[js.UndefOr[Int], js.Promise[js.Any]] = js.native
  var stopLoading: js.Function1[js.UndefOr[js.Promise[js.Any]], Unit] = js.native

  // miscellaneous functions
  var mainInit: js.Function0[Unit] = js.native
  var getAssetCode: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getAssetIcon: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getExchangeClass: js.Function1[js.UndefOr[String], String] = js.native
  var getTabIndex: js.Function0[Int] = js.native
  var normalizeExchange: js.Function1[js.UndefOr[String], String] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native

  var isOnline: js.Function1[js.UndefOr[UserProfile], Boolean] = js.native
  var getPreferenceIcon: js.Function1[js.Dynamic, String] = js.native
  var logout: js.Function0[Unit] = js.native
  var signIn: js.Function0[Unit] = js.native
  var signUp: js.Function0[Unit] = js.native

}