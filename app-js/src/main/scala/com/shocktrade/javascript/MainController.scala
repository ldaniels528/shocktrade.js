package com.shocktrade.javascript

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.{ScalaJsHelper, Controller, Angular}
import Angular.angular
import com.ldaniels528.javascript.angularjs.core._
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.core.GameLevels
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MainController._
import ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.dialogs.SignUpDialogService
import com.shocktrade.javascript.profile.ProfileService
import com.shocktrade.javascript.social.FacebookService

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Main Controller
 * @author lawrence.daniels@gmail.com
 */
class MainController($scope: js.Dynamic, $http: Http, $location: Location, $timeout: Timeout, toaster: Toaster,
                     @named("ContestService") contestService: ContestService,
                     @named("Facebook") facebook: FacebookService,
                     @named("MySession") mySession: MySession,
                     @named("ProfileService") profileService: ProfileService,
                     @named("SignUpDialog") signUpDialog: SignUpDialogService)
  extends Controller {

  private var loadingIndex = 0
  private var nonMember = true
  private val onlinePlayers = js.Dictionary[js.Dynamic]()

  ///////////////////////////////////////////////////////////////////////////
  //          Loading Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isLoading = () => loadingIndex > 0

  $scope.startLoading = (timeout: js.UndefOr[Int]) => startLoading(timeout)

  $scope.stopLoading = (promise: js.UndefOr[CancellablePromise]) => stopLoading(promise)

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.appTabs = appTabs

  $scope.levels = GameLevels.Levels

  $scope.mainInit = (uuid: String) => g.console.log(s"Session UUID is $uuid")

  $scope.changeAppTab = (tabIndex: js.UndefOr[Int]) => changeAppTab(tabIndex)

  $scope.getAssetCode = (q: js.Dynamic) => getAssetCode(q)

  $scope.getAssetIcon = (q: js.Dynamic) => getAssetIcon(q)

  $scope.getDate = (date: js.Dynamic) => if (isDefined(date) && isDefined(date.$date)) date.$date else date

  $scope.getExchangeClass = (exchange: js.UndefOr[String]) => s"${normalizeExchange(exchange)} bold"

  $scope.getHtmlQuote = (q: js.Dynamic) => if (!isDefined(q)) "" else s"<i class='${$scope.getAssetIcon(q)}'></i> ${q.symbol} - ${q.name}"

  $scope.isOnline = (player: js.Dynamic) => isOnline(player)

  $scope.getPreferenceIcon = (q: js.Dynamic) => getPreferenceIcon(q)

  $scope.getTabIndex = () => determineTableIndex

  $scope.isVisible = (tab: js.Dynamic) => (loadingIndex == 0) && ((!isTrue(tab.contestRequired) || mySession.contest.isDefined) && (!isTrue(tab.authenticationRequired) || mySession.isAuthenticated()))

  $scope.login = () => login()

  $scope.logout = () => logout()

  $scope.normalizeExchange = (market: js.UndefOr[String]) => normalizeExchange(market)

  $scope.postLoginUpdates = (facebookID: String, userInitiated: Boolean) => postLoginUpdates(facebookID, userInitiated)

  $scope.signUp = () => signUp()

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  private def isOnline(player: js.Dynamic): Boolean = {
    player.OID_? exists { playerID =>
      if (!onlinePlayers.contains(playerID)) {
        onlinePlayers(playerID) = JS(connected = false)
        profileService.getOnlineStatus(playerID) onComplete {
          case Success(newState) =>
            onlinePlayers(playerID) = newState
          case Failure(e) =>
            g.console.error(s"Error retrieving online state for user $playerID")
        }
      }
      val state = onlinePlayers(playerID)
      isDefined(state) && isDefined(state.connected) && state.connected.isTrue
    }
  }

  private def getPreferenceIcon(q: js.Dynamic): String = {
    // fail-safe
    if (!isDefined(q) || !isDefined(q.symbol)) ""
    else {
      // check for favorite and held securities
      val symbol = q.symbol.as[String]
      //if (heldSecurities.isHeld(symbol)) "fa fa-star"
      if (mySession.isFavoriteSymbol(symbol)) "fa fa-heart"
      else ""
    }
  }

  private def loadFacebookFriends() {
    g.console.log("Loading Facebook friends...")
    facebook.getTaggableFriends({ (response: js.Dynamic) =>
      if (isDefined(response.data)) {
        val friends = response.data.asArray[js.Dynamic]
        g.console.log(s"${friends.length} friend(s) loaded")
        friends.foreach(mySession.fbFriends.push(_))
      }
      ()
    })
  }

  private def login() {
    facebook.login() onComplete {
      case Success(response) =>
        nonMember = true

        // load the profile
        postLoginUpdates(facebook.facebookID, userInitiated = true)
      case Failure(e) =>
        g.console.error(s"main:login error")
        e.printStackTrace()
    }
  }

  private def logout() {
    nonMember = false
    facebook.logout()
    mySession.logout()
  }

  private def postLoginUpdates(facebookID: String, userInitiated: Boolean) = {
    g.console.log(s"facebookID = $facebookID, userInitiated = $userInitiated")

    // capture the Facebook user ID
    mySession.setFacebookID(facebookID)

    // load the user"s Facebook profile
    g.console.log(s"Retrieving Facebook profile for FBID $facebookID...")
    facebook.getUserProfile() onComplete {
      case Success(response) =>
        mySession.setFacebookProfile(response)
        facebook.profile = response
      case Failure(e) =>
        toaster.error(e.getMessage)
    }

    // load the user"s ShockTrade profile
    g.console.log(s"Retrieving ShockTrade profile for FBID $facebookID...")
    profileService.getProfileByFacebookID(facebookID) onComplete {
      case Success(profile) if isDefined(profile.error) =>
        nonMember = true
        g.console.log("Non-member identified.")
        if (userInitiated) signUpPopup(facebookID, mySession.fbProfile)
      case Success(profile) =>
        nonMember = false
        g.console.log("ShockTrade user profile loaded...")
        mySession.setUserProfile(profile, facebook.profile, facebookID)
        loadFacebookFriends()
      case Failure(e) =>
        toaster.error(s"ShockTrade Profile retrieval error - ${e.getMessage}")
    }
  }

  private def signUp(): Unit = signUpPopup(facebook.facebookID, Option(facebook.profile))

  private def signUpPopup(facebookID: String, fbProfile_? : Option[js.Dynamic]) {
    fbProfile_? map { fbProfile =>
      signUpDialog.popup(facebookID, fbProfile) onSuccess {
        case profile: js.Dynamic =>
          mySession.setUserProfile(profile, fbProfile, facebookID)
      }
    } getOrElse {
      g.console.log(s"facebookID = $facebookID, fbProfile = ${angular.toJson(fbProfile_?.orNull)}")
      toaster.info("Sign-in to Facebook, then Sign-up here")
    }
  }

  private def startLoading(timeout: js.UndefOr[Int]): CancellablePromise = {
    loadingIndex += 1
    val _timeout = timeout getOrElse DEFAULT_TIMEOUT

    // set loading timeout
    $timeout(() => {
      g.console.log(s"Disabling the loading animation due to time-out (${_timeout} msec)...")
      loadingIndex = 0
    }, _timeout)
  }

  private def stopLoading(promise: js.UndefOr[CancellablePromise] = js.undefined) = {
    $timeout.cancel(promise)
    $timeout(() => if (loadingIndex > 0) loadingIndex -= 1, 500.millis)
  }

  //////////////////////////////////////////////////////////////////////
  //              Tab Functions
  //////////////////////////////////////////////////////////////////////

  private def changeAppTab(index: js.UndefOr[Int]) = index foreach { tabIndex =>
    mySession.userProfile.OID_? match {
      case Some(userID) =>
        asyncLoading($scope)(profileService.setIsOnline(userID)) onComplete {
          case Success(outcome) =>
            if (isDefined(outcome.error)) {
              g.console.log(s"outcome = ${toJson(outcome)}")
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
    val tab = appTabs(tabIndex)
    g.console.log(s"Changing location for ${mySession.getUserName()} to ${tab.url}")
    $location.url(tab.url.as[String])
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

  $scope.$on(UserStatusChanged, (event: js.Dynamic, newState: js.Dynamic) => g.console.log(s"user_status_changed: newState = ${JSON.stringify(newState)}"))

}

/**
 * Main Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object MainController {
  private val DEFAULT_TIMEOUT = 15000

  private def getAssetCode(q: js.Dynamic): String = {
    if (!isDefined(q) || !isDefined(q.assetType)) ""
    else q.assetType.as[String] match {
      case "Crypto-Currency" => "&#xf15a" // fa-bitcoin
      case "Currency" => "&#xf155" // fa-dollar
      case "ETF" => "&#xf18d" // fa-stack-exchange
      case _ => "&#xf0ac" // fa-globe
    }
  }

  private def getAssetIcon(q: js.Dynamic): String = {
    if (!isDefined(q) || !isDefined(q.assetType)) "fa fa-globe st_blue"
    else q.assetType.as[String] match {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    }
  }

  private[javascript] def normalizeExchange(market: js.UndefOr[String]): String = {
    market map { myMarket =>
      if (myMarket == null) ""
      else {
        myMarket.toUpperCase match {
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
        }
      }
    } getOrElse ""
  }

  private val appTabs = js.Array(
    JS(name = "About", icon_class = "fa-info-circle", tool_tip = "About ShockTrade", url = "/about/us"),
    JS(name = "Home", icon_class = "fa-home", tool_tip = "My Home page", url = "/home", authenticationRequired = true),
    JS(name = "Search", icon_class = "fa-search", tool_tip = "Search for games", url = "/search"),
    JS(name = "Dashboard", icon_class = "fa-gamepad", tool_tip = "Main game dashboard", url = "/dashboard", contestRequired = true),
    JS(name = "Discover", icon_class = "fa-newspaper-o", tool_tip = "Stock News and Quotes", url = "/discover"),
    JS(name = "Explore", icon_class = "fa-trello", tool_tip = "Explore Sectors and Industries", url = "/explore"),
    JS(name = "Research", icon_class = "fa-database", tool_tip = "Stock Research", url = "/research"))

}