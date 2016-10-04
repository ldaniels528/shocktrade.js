package com.shocktrade.client

import com.shocktrade.common.models.quote.ClassifiedQuote
import com.shocktrade.common.models.OnlineStatus
import com.shocktrade.client.MainController._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.ContestService
import com.shocktrade.client.dialogs.SignUpDialog
import com.shocktrade.client.models.Profile
import com.shocktrade.client.profile.ProfileService
import org.scalajs.angularjs.facebook.FacebookService
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster._
import org.scalajs.angularjs.{Controller, Location, Scope, Timeout, injected, _}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.social.facebook.{FacebookProfileResponse, TaggableFriend}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Main Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class MainController($scope: MainControllerScope, $http: Http, $location: Location, $timeout: Timeout, toaster: Toaster,
                     @injected("ContestService") contestService: ContestService,
                     @injected("Facebook") facebook: FacebookService,
                     @injected("MySessionService") mySession: MySessionService,
                     @injected("ProfileService") profileService: ProfileService,
                     @injected("SignUpDialog") signUpDialog: SignUpDialog)
  extends Controller with GlobalLoading {

  private var loadingIndex = 0
  private var nonMember = true
  private val onlinePlayers = js.Dictionary[OnlineStatus]()

  $scope.appTabs = js.Array(
    new MainTab(name = "About", icon_class = "fa-info-circle", tool_tip = "About ShockTrade", url = "/about/us"),
    new MainTab(name = "Home", icon_class = "fa-home", tool_tip = "My Home page", url = "/home", authenticationRequired = true),
    new MainTab(name = "Search", icon_class = "fa-search", tool_tip = "Search for games", url = "/search"),
    new MainTab(name = "Dashboard", icon_class = "fa-gamepad", tool_tip = "Main game dashboard", url = "/dashboard", contestRequired = true),
    new MainTab(name = "Discover", icon_class = "fa-newspaper-o", tool_tip = "Stock News and Quotes", url = "/discover"),
    new MainTab(name = "Explore", icon_class = "fa-trello", tool_tip = "Explore Sectors and Industries", url = "/explore"),
    new MainTab(name = "Research", icon_class = "fa-database", tool_tip = "Stock Research", url = "/research"))

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

  $scope.getTabIndex = () => determineTableIndex

  $scope.isVisible = (aTab: js.UndefOr[ContestTab]) => aTab.exists { tab =>
    (loadingIndex == 0) && ((!tab.contestRequired || mySession.contest_?.isDefined) && (!tab.authenticationRequired || mySession.isAuthenticated))
  }

  $scope.normalizeExchange = (market: js.UndefOr[String]) => MainController.normalizeExchange(market)

  $scope.postLoginUpdates = (aFacebookID: js.UndefOr[String], aUserInitiated: js.UndefOr[Boolean]) => {
    for {
      facebookID <- aFacebookID
      userInitiated <- aUserInitiated
    } doPostLoginUpdates(facebookID, userInitiated)
  }

  //////////////////////////////////////////////////////////////////////
  //              My Session Service Functions
  //////////////////////////////////////////////////////////////////////

  $scope.contestIsEmpty = () => mySession.contest_?.isEmpty

  $scope.getContestID = () => mySession.getContestID

  $scope.getContestName = () => mySession.getContestName

  $scope.getContestStatus = () => mySession.getContestStatus

  $scope.getFacebookID = () => mySession.getFacebookID

  $scope.getFacebookProfile = () => mySession.getFacebookProfile

  $scope.getFacebookFriends = () => mySession.fbFriends_?

  $scope.getFundsAvailable = () => mySession.getFundsAvailable

  $scope.getNetWorth = () => mySession.getNetWorth.getOrElse(0d)

  $scope.getUserID = () => mySession.userProfile._id.orNull

  $scope.getUserName = () => mySession.getUserName.orNull

  $scope.getUserProfile = () => mySession.userProfile

  $scope.hasNotifications = () => mySession.hasNotifications

  $scope.hasPerk = (aPerkCode: js.UndefOr[String]) => {
    aPerkCode.exists(mySession.hasPerk)
  }

  $scope.isAdmin = () => mySession.isAdmin

  $scope.isAuthenticated = () => mySession.isAuthenticated

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  $scope.isOnline = (aPlayer: js.UndefOr[Profile]) => aPlayer.exists { player =>
    player._id.exists { playerID =>
      if (!onlinePlayers.contains(playerID)) {
        onlinePlayers(playerID) = new OnlineStatus(connected = false)
        profileService.getOnlineStatus(playerID) onComplete {
          case Success(newState) =>
            onlinePlayers(playerID) = newState
          case Failure(e) =>
            console.error(s"Error retrieving online state for user $playerID")
        }
      }
      onlinePlayers.get(playerID).exists(_.connected)
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

  $scope.login = () => {
    facebook.login() onComplete {
      case Success(response) =>
        nonMember = true

        // load the profile
        facebook.facebookID map (doPostLoginUpdates(_, userInitiated = true))
      case Failure(e) =>
        console.error(s"main:login error")
        e.printStackTrace()
    }
  }

  $scope.logout = () => {
    nonMember = false
    facebook.logout() onComplete {
      case Success(_) => mySession.logout()
      case Failure(e) =>
        toaster.error("An error occurred during logout")
        mySession.logout()
        e.printStackTrace()
    }
  }

  private def doPostLoginUpdates(facebookID: String, userInitiated: Boolean) = {
    console.log(s"facebookID = $facebookID, userInitiated = $userInitiated")

    // capture the Facebook user ID
    mySession.setFacebookID(facebookID)

    val outcome = for {
    // load the user"s Facebook profile
      fbProfile <- {
        console.log(s"Retrieving Facebook profile for FBID $facebookID...")
        facebook.getUserProfile
      }

      fbFriends <- {
        console.log(s"Loading Facebook friends for FBID $facebookID...")
        facebook.getTaggableFriends
      }

      // load the user"s ShockTrade profile
      profile <- {
        console.log(s"Retrieving ShockTrade profile for FBID $facebookID...")
        profileService.getProfileByFacebookID(facebookID)
      }
    } yield (fbProfile, fbFriends, profile)

    outcome onComplete {
      case Success((fbProfile, fbFriends, profile)) =>
        console.log("ShockTrade user profile, Facebook profile, and friends loaded...")
        nonMember = false
        mySession.setUserProfile(profile, fbProfile)
        mySession.fbFriends_? = fbFriends
      case Failure(e) =>
        toaster.error(s"ShockTrade Profile retrieval error - ${e.getMessage}")
    }
  }

  $scope.signUp = () => {
    signUpDialog.popup() onComplete {
      case Success((profile, fbProfile)) =>
        mySession.setUserProfile(profile, fbProfile)
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              Tab Functions
  //////////////////////////////////////////////////////////////////////

  $scope.changeAppTab = (index: js.UndefOr[Int]) => index foreach { tabIndex =>
    mySession.userProfile._id.toOption match {
      case Some(userID) =>
        asyncLoading($scope)(profileService.setIsOnline(userID)) onComplete {
          case Success(outcome) =>
            if (isDefined(outcome.error)) {
              console.log(s"outcome = ${angular.toJson(outcome)}")
              toaster.error(outcome.error)
            }
            performTabSwitch(tabIndex)
          case Failure(e) =>
            toaster.error(e.getMessage)
        }
      case None =>
        performTabSwitch(tabIndex)
    }
  }

  private def performTabSwitch(tabIndex: Int): Unit = {
    val tab = $scope.appTabs(tabIndex)
    console.log(s"Changing location for ${mySession.getUserName} to ${tab.url}")
    $location.url(tab.url)
  }

  private def determineTableIndex: Int = $location.path() match {
    case path if path.contains("/about") => 0
    case path if path.contains("/home") => 1
    case path if path.contains("/search") => 2
    case path if path.contains("/dashboard") => 3
    case path if path.contains("/discover") => 4
    case path if path.contains("/explore") => 5
    case path if path.contains("/research") => 6
    case path => 0
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

  protected[client] def getAssetCode(q: js.UndefOr[ClassifiedQuote]) = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "&#xf15a" // fa-bitcoin
      case "Currency" => "&#xf155" // fa-dollar
      case "ETF" => "&#xf18d" // fa-stack-exchange
      case _ => "&#xf0ac" // fa-globe
    } getOrElse ""
  }

  protected[client] def getAssetIcon(q: js.UndefOr[ClassifiedQuote]) = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    } getOrElse "fa fa-globe st_blue"
  }

  protected[client] def normalizeExchange(aMarket: js.UndefOr[String]) = {
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

  /**
    * Represents a Contest Tab
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @js.native
  trait ContestTab extends js.Object {
    var contestRequired: Boolean = js.native
    var authenticationRequired: Boolean = js.native
  }

  /**
    * Represents a Main Tab
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class MainTab(val name: String,
                val icon_class: String,
                val tool_tip: String,
                val url: String,
                val contestRequired: Boolean = false,
                val authenticationRequired: Boolean = false) extends js.Object

}

/**
  * Main Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait MainControllerScope extends Scope {
  // variables
  var appTabs: js.Array[MainTab] = js.native
  var levels: js.Array[GameLevel] = js.native

  // functions
  var isLoading: js.Function0[Boolean]
  var startLoading: js.Function1[js.UndefOr[Int], js.Promise[js.Any]] = js.native
  var stopLoading: js.Function1[js.UndefOr[js.Promise[js.Any]], Unit] = js.native

  var mainInit: js.Function0[Unit] = js.native
  var getAssetCode: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getAssetIcon: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getExchangeClass: js.Function1[js.UndefOr[String], String] = js.native
  var getTabIndex: js.Function0[Int] = js.native
  var isVisible: js.Function1[js.UndefOr[ContestTab], Boolean] = js.native
  var normalizeExchange: js.Function1[js.UndefOr[String], String] = js.native
  var postLoginUpdates: js.Function2[js.UndefOr[String], js.UndefOr[Boolean], Unit] = js.native

  var contestIsEmpty: js.Function0[Boolean] = js.native
  var getContestID: js.Function0[js.UndefOr[String]] = js.native
  var getContestName: js.Function0[String] = js.native
  var getContestStatus: js.Function0[String] = js.native
  var getFacebookID: js.Function0[js.UndefOr[String]] = js.native
  var getFacebookProfile: js.Function0[js.UndefOr[FacebookProfileResponse]] = js.native
  var getFacebookFriends: js.Function0[js.Array[TaggableFriend]] = js.native
  var getFundsAvailable: js.Function0[Double] = js.native
  var getNetWorth: js.Function0[Double] = js.native
  var getUserID: js.Function0[String] = js.native
  var getUserName: js.Function0[String] = js.native
  var getUserProfile: js.Function0[Profile] = js.native
  var hasNotifications: js.Function0[Boolean] = js.native
  var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
  var isAdmin: js.Function0[Boolean] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native

  var changeAppTab: js.Function1[js.UndefOr[Int], Unit] = js.native
  var isOnline: js.Function1[js.UndefOr[Profile], Boolean] = js.native
  var getPreferenceIcon: js.Function1[js.Dynamic, String] = js.native
  var login: js.Function0[Unit] = js.native
  var logout: js.Function0[Unit] = js.native
  var signUp: js.Function0[Unit] = js.native

}