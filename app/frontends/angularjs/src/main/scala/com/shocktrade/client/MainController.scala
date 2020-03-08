package com.shocktrade.client

import com.shocktrade.client.MainController._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ContestService, GameLevel}
import com.shocktrade.client.dialogs.SignUpDialogController
import com.shocktrade.client.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.SignInDialogController.SignInDialogResult
import com.shocktrade.client.users.{AuthenticationService, SignInDialogController, UserService}
import com.shocktrade.common.models.quote.ClassifiedQuote
import com.shocktrade.common.models.user.OnlineStatus
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster._
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalOptions}
import io.scalajs.npm.angularjs.{Controller, Location, Scope, Timeout, injected, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Main Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class MainController($scope: MainControllerScope, $http: Http, $location: Location, $timeout: Timeout, toaster: Toaster, $uibModal: Modal,
                     @injected("ContestService") contestService: ContestService,
                     @injected("AuthenticationService") authenticationService: AuthenticationService,
                     @injected("MySessionService") mySession: MySessionService,
                     @injected("UserService") profileService: UserService)
  extends Controller with GlobalLoading {

  private var loadingIndex = 0
  private val onlinePlayers = js.Dictionary[OnlineStatus]()

  $scope.appTabs = MainTab.Tabs
  $scope.levels = GameLevel.Levels

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

  $scope.mainInit = () => {
    console.log(s"Initializing ${getClass.getSimpleName}...")
  }

  $scope.getAssetCode = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetCode(q)

  $scope.getAssetIcon = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetIcon(q)

  $scope.getExchangeClass = (exchange: js.UndefOr[String]) => s"${normalizeExchange(exchange)} bold"

  $scope.getTabIndex = () => $scope.appTabs.indexWhere(tab => $location.path.contains(tab.url))

  $scope.normalizeExchange = (market: js.UndefOr[String]) => MainController.normalizeExchange(market)

  $scope.postLoginUpdates = (aFacebookID: js.UndefOr[String], aUserInitiated: js.UndefOr[Boolean]) => {
    for {
      facebookID <- aFacebookID
      userInitiated <- aUserInitiated
    } {
      mySession.doPostLoginUpdates(facebookID, userInitiated)
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              My Session Service Functions
  //////////////////////////////////////////////////////////////////////

  $scope.contestIsEmpty = () => mySession.contest_?.isEmpty

  $scope.getContestID = () => mySession.getContestID

  $scope.getContestName = () => mySession.getContestName

  $scope.getContestStatus = () => mySession.getContestStatus

  $scope.getFundsAvailable = () => mySession.getFundsAvailable

  $scope.getTotalInvestment = () => $scope.getNetWorth() - $scope.getWallet()

  $scope.getNetWorth = () => mySession.userProfile.netWorth.orZero

  $scope.getWallet = () => mySession.userProfile.wallet.orZero

  $scope.getUserID = () => mySession.userProfile.userID.orNull

  $scope.getUserName = () => mySession.getUserName.orNull

  $scope.getUserProfile = () => mySession.userProfile

  $scope.hasNotifications = () => mySession.hasNotifications

  $scope.hasPerk = (aPerkCode: js.UndefOr[String]) => aPerkCode.exists(mySession.hasPerk)

  $scope.isAdmin = () => mySession.isAdmin

  $scope.isAuthenticated = () => mySession.isAuthenticated

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  $scope.isOnline = (aPlayer: js.UndefOr[UserProfile]) => aPlayer.exists { player =>
    player.userID.exists { portfolioID =>
      if (!onlinePlayers.contains(portfolioID)) {
        onlinePlayers(portfolioID) = new OnlineStatus(connected = false)
        profileService.getOnlineStatus(portfolioID) onComplete {
          case Success(response) =>
            val newState = response.data
            onlinePlayers(portfolioID) = newState
          case Failure(e) =>
            console.error(s"Error retrieving online state for user $portfolioID: ${e.getMessage}")
        }
      }
      onlinePlayers.get(portfolioID).exists(_.connected)
    }
  }

  $scope.getPreferenceIcon = (q: js.Dynamic) => {
    // fail-safe
    if (!isDefined(q) || !isDefined(q.symbol)) ""
    else {
      // check for favorite and held securities
      q.symbol.asOpt[String] map { symbol =>
        if (mySession.isFavoriteSymbol(symbol)) "fa fa-heart"
        //else if (heldSecurities.isHeld(symbol)) "fa fa-star"
        else ""
      } getOrElse ""
    }
  }

  $scope.logout = () => {
    authenticationService.logout() onComplete {
      case Success(_) => mySession.logout()
      case Failure(e) =>
        toaster.error("An error occurred during logout")
        mySession.logout()
        e.printStackTrace()
    }
  }

  $scope.signIn = () => {
    val modalInstance = $uibModal.open[SignInDialogResult](new ModalOptions(
      templateUrl = "sign_in_dialog.html",
      controller = classOf[SignInDialogController].getSimpleName
    ))
    modalInstance.result onComplete {
      case Success(response) =>
        console.log(s"response = ${JSON.stringify(response)}")
        mySession.setUserProfile(response)
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  $scope.signUp = () => {
    val modalInstance = $uibModal.open[SignUpDialogResult](new ModalOptions(
      controller = classOf[SignUpDialogController].getSimpleName,
      templateUrl = "sign_up_dialog.html"
    ))
    modalInstance.result onComplete {
      case Success(profile) =>
        mySession.setUserProfile(profile)
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Tab-related Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isVisibleTab = (aTab: js.UndefOr[MainTab]) => aTab.exists { tab =>
    (loadingIndex == 0) && ((!tab.contestRequired || mySession.contest_?.isDefined) && (!tab.authenticationRequired || mySession.isAuthenticated))
  }

  $scope.switchToDiscover = () => $scope.switchToTab(MainTab.Discover)

  $scope.switchToGameSearch = () => $scope.switchToTab(MainTab.Search)

  $scope.switchToHome = () => $scope.switchToTab(MainTab.Home)

  $scope.switchToNewsFeed = () => $scope.switchToTab(MainTab.NewsFeed)

  $scope.switchToTab = (index: js.UndefOr[Int]) => index foreach { tabIndex =>
    mySession.userProfile.userID.toOption match {
      case Some(userID) =>
        asyncLoading($scope)(profileService.setIsOnline(userID)) onComplete {
          case Success(response) =>
            val outcome = response.data
            if (isDefined(outcome.error)) {
              console.log(s"outcome = ${angular.toJson(outcome)}")
              toaster.error(outcome.error.toString)
            }
            performTabSwitch(tabIndex)
          case Failure(e) =>
            toaster.error(e.getMessage)
        }
      case None =>
        performTabSwitch(tabIndex)
    }
  }

  private def performTabSwitch(tabIndex: Int) {
    val tab = MainTab.Tabs(tabIndex)
    console.log(s"Changing location for ${mySession.getUserName.orNull} to ${tab.url}")
    $location.url(tab.url)
  }

  //////////////////////////////////////////////////////////////////////
  //              Event Listeners
  //////////////////////////////////////////////////////////////////////

  $scope.onUserStatusChanged((_, newState) => console.log(s"user_status_changed: newState = ${JSON.stringify(newState)}"))

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
trait MainControllerScope extends Scope with GlobalNavigation {
  // variables
  var appTabs: js.Array[MainTab] = js.native
  var levels: js.Array[GameLevel] = js.native

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
  var postLoginUpdates: js.Function2[js.UndefOr[String], js.UndefOr[Boolean], Unit] = js.native

  var contestIsEmpty: js.Function0[Boolean] = js.native
  var getContestID: js.Function0[js.UndefOr[String]] = js.native
  var getContestName: js.Function0[String] = js.native
  var getContestStatus: js.Function0[String] = js.native

  var getFundsAvailable: js.Function0[Double] = js.native
  var getTotalInvestment: js.Function0[Double] = js.native
  var getNetWorth: js.Function0[Double] = js.native
  var getWallet: js.Function0[Double] = js.native
  var getUserID: js.Function0[String] = js.native
  var getUserName: js.Function0[String] = js.native
  var getUserProfile: js.Function0[UserProfile] = js.native
  var hasNotifications: js.Function0[Boolean] = js.native
  var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
  var isAdmin: js.Function0[Boolean] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native

  var isOnline: js.Function1[js.UndefOr[UserProfile], Boolean] = js.native
  var getPreferenceIcon: js.Function1[js.Dynamic, String] = js.native
  var logout: js.Function0[Unit] = js.native
  var signIn: js.Function0[Unit] = js.native
  var signUp: js.Function0[Unit] = js.native

}