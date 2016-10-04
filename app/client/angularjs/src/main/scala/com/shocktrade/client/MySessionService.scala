package com.shocktrade.client

import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ChatService, ContestRankingCapability, ContestService, PortfolioService}
import com.shocktrade.client.models.Profile
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.client.profile.ProfileService
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.facebook.FacebookService
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.console
import org.scalajs.nodejs.social.facebook.{FacebookProfileResponse, TaggableFriend}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * My Session Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class MySessionService($rootScope: Scope, $timeout: Timeout, toaster: Toaster,
                            @injected("Facebook") facebook: FacebookService,
                            @injected("ChatService") chatService: ChatService,
                            @injected("ContestService") contestService: ContestService,
                            @injected("PortfolioService") portfolioService: PortfolioService,
                            @injected("ProfileService") profileService: ProfileService,
                            @injected("QuoteCache") quoteCache: QuoteCache)
  extends Service with ContestRankingCapability {

  private val notifications = emptyArray[String]
  var facebookID: js.UndefOr[String] = js.undefined
  var fbFriends_? = emptyArray[TaggableFriend]
  var fbProfile_? : js.UndefOr[FacebookProfileResponse] = js.undefined

  var contest_? : Option[Contest] = None
  var portfolio_? : Option[Portfolio] = None
  var participant_? : Option[Participant] = None
  var userProfile: Profile = createSpectatorProfile()

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def setUserProfile(profile: Profile, profileFB: FacebookProfileResponse) {
    this.userProfile = profile
    this.fbProfile_? = profileFB
    this.facebookID = fbProfile_?.map(_.id)

    console.info(s"profile = ${angular.toJson(profile)}, facebookID = $facebookID, profileFB = ${angular.toJson(profileFB)}")

    // broadcast the user profile change event
    $rootScope.emitUserProfileChanged(profile)
    ()
  }

  /**
    * Returns the user ID for the current user's name
    * @return {*}
    */
  def getUserName = userProfile.name

  /**
    * Indicates whether the given user is an administrator
    * @return {boolean}
    */
  def isAdmin = userProfile.isAdmin.isTrue

  /**
    * Indicates whether the user is logged in
    * @return {boolean}
    */
  def isAuthenticated = userProfile._id.isAssigned

  def getFacebookID = facebookID

  def setFacebookID(fbId: String) = facebookID = fbId

  def getFacebookProfile = fbProfile_?

  def setFacebookProfile(profile: FacebookProfileResponse) = fbProfile_? = profile

  def isFbAuthenticated = fbProfile_?.isDefined

  /**
    * Logout function
    */
  def logout() = {
    facebookID = js.undefined
    fbFriends_? = js.Array[TaggableFriend]()
    fbProfile_? = js.undefined
    userProfile = createSpectatorProfile()
    resetContest()
  }

  def refresh() = {
    facebookID.foreach { fbId =>
      profileService.getProfileByFacebookID(fbId) onComplete {
        case Success(profile) =>
          userProfile.netWorth = profile.netWorth
        case Failure(e) =>
          toaster.error("Error loading user profile");
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          NetWorth Functions
  /////////////////////////////////////////////////////////////////////

  def deduct(amount: Double) = {
    console.log(f"Deducting $amount%.2f from ${userProfile.netWorth}")
    userProfile.netWorth = userProfile.netWorth.map(_ - amount)
  }

  def getNetWorth = userProfile.netWorth

  /////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites, Recent, etc.
  /////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(symbol: String) = userProfile._id foreach (id => profileService.addFavoriteSymbol(id, symbol))

  def getFavoriteSymbols = userProfile.favoriteSymbols

  def isFavoriteSymbol(symbol: String) = getFavoriteSymbols.exists(_.contains(symbol))

  def removeFavoriteSymbol(symbol: String) = userProfile._id foreach (id => profileService.removeFavoriteSymbol(id, symbol))

  def addRecentSymbol(symbol: String) = userProfile._id foreach (id => profileService.addRecentSymbol(id, symbol))

  def getRecentSymbols = userProfile.recentSymbols

  def isRecentSymbol(symbol: String) = getRecentSymbols.exists(_.contains(symbol))

  def removeRecentSymbol(symbol: String) = userProfile._id foreach (id => profileService.removeRecentSymbol(id, symbol))

  def getMostRecentSymbol = getRecentSymbols.toOption.flatMap(_.lastOption) getOrElse "AAPL"

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def getContest = contest_? getOrElse JS()

  def getContestID = contest_?.orUndefined.flatMap(_._id)

  def getContestName = contest_?.flatMap(_.name.toOption).orNull

  def getContestStatus = contest_?.flatMap(_.status.toOption).orNull

  def loadContestByID(contestId: String)(implicit ec: ExecutionContext) = {
    Future.sequence {
      userProfile._id map { playerId =>
        console.log(s"Loading contest $contestId (player: $playerId)...")
        for {
          contest <- contestService.getContestByID(contestId)
          portfolio <- portfolioService.getPortfolioByPlayer(contestId, playerId)
          participantOpt = contest.participants.toOption.flatMap(_.find(_.is(playerId)))
        } yield {
          // set the variables
          contest_? = Option(contest)
          participant_? = participantOpt
          portfolio_? = Option(portfolio)

          // broadcast the event
          $rootScope.emitContestSelected(contest)

          // return the tuple
          (contest, portfolio, playerId, participantOpt)
        }
      } toList
    } map (_.headOption)
  }

  def updatePortfolio(portfolio: Portfolio) = {
    console.log(s"portfolio = ${angular.toJson(portfolio)}")
    portfolio_? = Option(portfolio)
  }

  def setContest(contest: Contest) = {
    contest_? = Option(contest)

    // if the player is defined ...
    for {
      contestId <- contest._id
      playerId <- userProfile._id
    } {
      // attempt to find the participant
      participant_? = for {
        playerId <- userProfile._id.toOption
        participant <- contest.participants.toOption.flatMap(_.find(_.is(playerId)))
      } yield participant

      // lookup the portfolio
      portfolioService.getPortfolioByPlayer(contestId, playerId) foreach { portfolio =>
        portfolio_? = Option(portfolio)
      }
    }

    console.log(s"setContest: contest = %s, participant = %s, portfolio = %s",
      angular.toJson(contest), angular.toJson(participant_?.orNull), angular.toJson(portfolio_?.orNull))
  }

  /**
    * Returns the combined total funds for both the cash and margin accounts
    */
  def getCompleteFundsAvailable = {
    for {
      cashAccount <- cashAccount_?
      cashFunds = cashAccount.cashFunds getOrElse 0.00d
      marginAccount <- marginAccount_?
      marginFunds = marginAccount.cashFunds getOrElse 0.00d
    } yield cashFunds + marginFunds
  }

  def getFundsAvailable = cashAccount_?.orUndefined.flatMap(_.cashFunds) getOrElse 0.00d

  def deductFundsAvailable(amount: Double) = {
    portfolio_? foreach { portfolio =>
      console.log(s"Deducting funds: $amount from ${portfolio.cashAccount.flatMap(_.cashFunds)}")
      portfolio.cashAccount.foreach(acct => acct.cashFunds = acct.cashFunds.map(_ - amount))
      // TODO rethink this
    }
    ()
  }

  def getMessages = contest_?.orUndefined.flatMap(_.messages.flat) getOrElse emptyArray[ChatMessage]

  def setMessages(messages: js.Array[ChatMessage]) = contest_?.foreach(_.messages = messages)

  def getMyAwards = userProfile.awards getOrElse emptyArray

  def getOrders = portfolio_?.orUndefined.flatMap(_.orders) getOrElse emptyArray

  def getClosedOrders = portfolio_?.orUndefined.flatMap(_.closedOrders) getOrElse emptyArray

  def getPerformance = portfolio_?.orUndefined.flatMap(_.performance) getOrElse emptyArray

  def getPerks = portfolio_?.orUndefined.flatMap(_.perks) getOrElse emptyArray

  def hasPerk(perkCode: String) = getPerks.contains(perkCode)

  def getPositions = portfolio_?.orUndefined.flatMap(_.positions) getOrElse emptyArray

  def resetContest() = {
    contest_? = None
    participant_? = None
    portfolio_? = None
  }

  ////////////////////////////////////////////////////////////
  //          Social Network Methods
  ////////////////////////////////////////////////////////////

  def doFacebookLogin() = {
    // perform the login
    facebook.getLoginStatus onComplete {
      case Success(response) =>
        console.log("Facebook login successful.")
        initFacebook(facebookID = response.authResponse.userID, userInitiated = false)
      case Failure(e) =>
        console.error(s"Facebook: ${e.displayMessage}")
    }
  }

  def initFacebook(facebookID: String, userInitiated: Boolean) = {
    console.log(s"facebookID = $facebookID, userInitiated = $userInitiated")

    // capture the Facebook user ID
    this.facebookID = facebookID

    val outcome = for {
    // load the user's ShockTrade profile
      profile <- {
        console.log(s"Retrieving ShockTrade profile for FBID $facebookID...")
        profileService.getProfileByFacebookID(facebookID)
      }

      // load the user"s Facebook profile
      fbProfile <- {
        console.log(s"Retrieving Facebook profile for FBID $facebookID...")
        facebook.getUserProfile
      }

      fbFriends <- {
        console.log(s"Loading Facebook friends for FBID $facebookID...")
        facebook.getTaggableFriends
      }

      fbCloseFriends <- {
        console.log(s"Loading Facebook close friends for FBID $facebookID...")
        facebook.getFriendList()
      }

    } yield (profile, fbProfile, fbFriends, fbCloseFriends)

    outcome onComplete {
      case Success((profile, fbProfile, fbFriends, fbCloseFriends)) =>
        console.log("ShockTrade user profile, Facebook profile, and friends loaded...")
        console.log(s"fbCloseFriends = ${angular.toJson(fbCloseFriends, pretty = true)}")
        this.fbProfile_? = fbProfile
        this.fbFriends_? = fbFriends
        setUserProfile(profile, fbProfile)
      case Failure(e) =>
        toaster.error(s"Profile retrieval error - ${e.displayMessage}")
        console.error(s"Profile retrieval error - ${e.displayMessage}")
    }
    ()
  }

  def isFacebookConnected = facebookID.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Notification Methods
  ////////////////////////////////////////////////////////////

  def addNotification(message: String) = {
    while (notifications.push(message) > 20) notifications.shift()
    notifications
  }

  def getNotifications = notifications

  def hasNotifications = notifications.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Participant Methods
  ////////////////////////////////////////////////////////////

  def findPlayerByID(contest: Contest, playerId: String) = {
    if (isDefined(contest) && isDefined(contest.participants))
      contest.participants.toOption.flatMap(_.find(_._id.contains(playerId)))
    else
      None
  }

  ////////////////////////////////////////////////////////////
  //          Private Methods
  ////////////////////////////////////////////////////////////

  /**
    * Creates a default 'Spectator' user profile
    * @return {{name: string, country: string, level: number, lastSymbol: string, friends: Array, filters: *[]}}
    */
  private def createSpectatorProfile() = {
    notifications.removeAll()
    facebookID = js.undefined
    fbFriends_? = emptyArray[TaggableFriend]
    fbProfile_? = js.undefined
    resetContest()

    new Profile(
      name = "Spectator",
      country = "us",
      lastSymbol = "AAPL"
    )
  }

  private[client] def cashAccount_? = portfolio_?.flatMap(_.cashAccount.toOption)

  private[client] def marginAccount_? = portfolio_?.flatMap(_.marginAccount.toOption)

  /////////////////////////////////////////////////////////////////////////////
  //			Events
  /////////////////////////////////////////////////////////////////////////////

  $rootScope.onUserProfileUpdated { (_, profile) =>
    console.log(s"User Profile for ${profile.name} updated")
    for {
      userId <- userProfile._id
      otherId <- profile._id
    } if (userId == otherId) {
      userProfile.netWorth = profile.netWorth
      toaster.success("Your Wallet", s"<ul><li>Your wallet now has $$${profile.netWorth}</li></ul>", 5.seconds, "trustedHtml")
    }
  }

  private def info(contest: Contest, message: String) = console.log(s"${contest.name}: $message")

}
