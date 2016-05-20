package com.shocktrade.javascript

import com.github.ldaniels528.meansjs.angularjs._
import com.github.ldaniels528.meansjs.angularjs.facebook.FacebookService
import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.angularjs.{Controller, Location, Scope, Timeout, injected}
import com.github.ldaniels528.meansjs.angularjs.toaster._
import com.github.ldaniels528.meansjs.social.facebook.{FacebookProfileResponse, TaggableFriend}
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MainController._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.dialogs.SignUpDialog
import com.shocktrade.javascript.models.{BSONObjectID, ClassifiedQuote, OnlinePlayerState, UserProfile}
import com.shocktrade.javascript.profile.ProfileService
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  * Main Controller
  * @author lawrence.daniels@gmail.com
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
  private val onlinePlayers = js.Dictionary[OnlinePlayerState]()

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

  $scope.mainInit = (uuid: js.UndefOr[String]) => console.log(s"Session UUID is $uuid")

  $scope.getAssetCode = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetCode(q)

  $scope.getAssetIcon = (q: js.UndefOr[ClassifiedQuote]) => MainController.getAssetIcon(q)

  $scope.getDate = (date: js.Dynamic) => if (isDefined(date) && isDefined(date.$date)) date.$date else date

  $scope.getExchangeClass = (exchange: js.UndefOr[String]) => s"${normalizeExchange(exchange)} bold"

  $scope.getTabIndex = () => determineTableIndex

  $scope.isVisible = (aTab: js.UndefOr[ContestTab]) => aTab.exists { tab =>
    (loadingIndex == 0) && ((!tab.contestRequired || mySession.contest.isDefined) && (!tab.authenticationRequired || mySession.isAuthenticated))
  }

  $scope.normalizeExchange = (market: js.UndefOr[String]) => MainController.normalizeExchange(market)

  $scope.postLoginUpdates = (aFacebookID: js.UndefOr[String], aUserInitiated: js.UndefOr[Boolean]) => {
    for {
      facebookID <- aFacebookID
      userInitiated <- aUserInitiated
    } doPostLoginUpdates(facebookID, userInitiated)
  }

  //////////////////////////////////////////////////////////////////////
  //              MySessionService Functions
  //////////////////////////////////////////////////////////////////////

  $scope.contestIsEmpty = () => mySession.contest.isEmpty

  $scope.getContestID = () => mySession.getContestID

  $scope.getContestName = () => mySession.getContestName

  $scope.getContestStatus = () => mySession.getContestStatus

  $scope.getFacebookID = () => mySession.getFacebookID

  $scope.getFacebookProfile = () => mySession.getFacebookProfile

  $scope.getFacebookFriends = () => mySession.fbFriends

  $scope.getFundsAvailable = () => mySession.getFundsAvailable

  $scope.getNetWorth = () => mySession.getNetWorth

  $scope.getUserID = () => mySession.userProfile._id.orNull

  $scope.getUserName = () => mySession.getUserName

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

  $scope.isOnline = (aPlayer: js.UndefOr[UserProfile]) => aPlayer.exists { player =>
    player._id.map(_.$oid) exists { playerID =>
      if (!onlinePlayers.contains(playerID)) {
        onlinePlayers(playerID) = OnlinePlayerState(connected = false)
        profileService.getOnlineStatus(BSONObjectID(playerID)) onComplete {
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
        mySession.fbFriends = fbFriends
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
    val tab = MainTab.Tabs(tabIndex)
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

  $scope.$on(UserStatusChanged, (event: js.Dynamic, newState: js.Dynamic) =>
    console.log(s"user_status_changed: newState = ${JSON.stringify(newState)}"))

}

/**
  * Main Controller Singleton
  * @author lawrence.daniels@gmail.com
  */
object MainController {
  private val DEFAULT_TIMEOUT = 15000

  protected[javascript] def getAssetCode(q: js.UndefOr[ClassifiedQuote]) = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "&#xf15a" // fa-bitcoin
      case "Currency" => "&#xf155" // fa-dollar
      case "ETF" => "&#xf18d" // fa-stack-exchange
      case _ => "&#xf0ac" // fa-globe
    } getOrElse ""
  }

  protected[javascript] def getAssetIcon(q: js.UndefOr[ClassifiedQuote]) = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    } getOrElse "fa fa-globe st_blue"
  }

  protected[javascript] def normalizeExchange(aMarket: js.UndefOr[String]) = {
    aMarket map (_.toUpperCase) map {
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
  * @author lawrence.daniels@gmail.com
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

  var mainInit: js.Function1[js.UndefOr[String], Unit] = js.native
  var getAssetCode: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getAssetIcon: js.Function1[js.UndefOr[ClassifiedQuote], String] = js.native
  var getDate: js.Function1[js.Dynamic, js.Dynamic] = js.native
  var getExchangeClass: js.Function1[js.UndefOr[String], String] = js.native
  var getTabIndex: js.Function0[Int] = js.native
  var isVisible: js.Function1[js.UndefOr[ContestTab], Boolean] = js.native
  var normalizeExchange: js.Function1[js.UndefOr[String], String] = js.native
  var postLoginUpdates: js.Function2[js.UndefOr[String], js.UndefOr[Boolean], Unit] = js.native

  var contestIsEmpty: js.Function0[Boolean] = js.native
  var getContestID: js.Function0[js.UndefOr[BSONObjectID]] = js.native
  var getContestName: js.Function0[String] = js.native
  var getContestStatus: js.Function0[String] = js.native
  var getFacebookID: js.Function0[String] = js.native
  var getFacebookProfile: js.Function0[js.UndefOr[FacebookProfileResponse]] = js.native
  var getFacebookFriends: js.Function0[js.Array[TaggableFriend]] = js.native
  var getFundsAvailable: js.Function0[Double] = js.native
  var getNetWorth: js.Function0[Double] = js.native
  var getUserID: js.Function0[BSONObjectID] = js.native
  var getUserName: js.Function0[String] = js.native
  var getUserProfile: js.Function0[UserProfile] = js.native
  var hasNotifications: js.Function0[Boolean] = js.native
  var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
  var isAdmin: js.Function0[Boolean] = js.native
  var isAuthenticated: js.Function0[Boolean] = js.native

  var changeAppTab: js.Function1[js.UndefOr[Int], Unit] = js.native
  var isOnline: js.Function1[js.UndefOr[UserProfile], Boolean] = js.native
  var getPreferenceIcon: js.Function1[js.Dynamic, String] = js.native
  var login: js.Function0[Unit] = js.native
  var logout: js.Function0[Unit] = js.native
  var signUp: js.Function0[Unit] = js.native

}

@js.native
trait ContestTab extends js.Object {
  var contestRequired: Boolean = js.native
  var authenticationRequired: Boolean = js.native
}