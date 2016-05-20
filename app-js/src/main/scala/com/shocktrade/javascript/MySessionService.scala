package com.shocktrade.javascript

import com.github.ldaniels528.meansjs.angularjs.AngularJsHelper._
import com.github.ldaniels528.meansjs.angularjs.facebook.FacebookService
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.angularjs.{Timeout, _}
import com.github.ldaniels528.meansjs.social.facebook.{FacebookProfileResponse, TaggableFriend}
import com.github.ldaniels528.meansjs.social.linkedin.LinkedIn
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.models._
import com.shocktrade.javascript.profile.ProfileService
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * My Session Service
  * @author lawrence.daniels@gmail.com
  */
class MySessionService($rootScope: Scope, $timeout: Timeout, toaster: Toaster,
                       @injected("Facebook") facebook: FacebookService,
                       @injected("ContestService") contestService: ContestService,
                       @injected("ProfileService") profileService: ProfileService)
  extends Service {

  val notifications = emptyArray[String]
  var facebookID: js.UndefOr[String] = js.undefined
  var linkedInID: js.UndefOr[String] = js.undefined
  var fbFriends = emptyArray[TaggableFriend]
  var fbProfile: js.UndefOr[FacebookProfileResponse] = js.undefined
  var contest: Option[Contest] = None
  var userProfile: UserProfile = createSpectatorProfile()

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def setUserProfile(profile: UserProfile, profileFB: FacebookProfileResponse) {
    console.log(s"facebookID = $facebookID, profileFB = ${angular.toJson(profileFB)}")

    this.fbProfile = profileFB
    this.facebookID = fbProfile.map(_.id)

    console.log(s"profile = ${angular.toJson(profile)}")
    this.userProfile = profile

    // broadcast the user profile change event
    $rootScope.$broadcast(UserProfileChanged, profile)
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
  def isAdmin = userProfile.admin.getOrElse(false)

  /**
    * Indicates whether the user is logged in
    * @return {boolean}
    */
  def isAuthenticated = userProfile._id.exists(_.$oid.nonEmpty)

  def getFacebookID = facebookID.orNull

  def setFacebookID(fbId: String) = facebookID = fbId

  def getFacebookProfile = fbProfile

  def setFacebookProfile(profile: FacebookProfileResponse) = fbProfile = profile

  def isFbAuthenticated = fbProfile.isDefined

  /**
    * Logout function
    */
  def logout() = {
    facebookID = js.undefined
    fbFriends = js.Array[TaggableFriend]()
    fbProfile = js.undefined
    userProfile = createSpectatorProfile()
    contest = None
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
    userProfile.netWorth -= amount
  }

  def getNetWorth = userProfile.netWorth

  /////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites, Recent, etc.
  /////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(symbol: String) = userProfile._id foreach (id => profileService.addFavoriteSymbol(id, symbol))

  def getFavoriteSymbols = userProfile.favorites

  def isFavoriteSymbol(symbol: String) = getFavoriteSymbols.contains(symbol)

  def removeFavoriteSymbol(symbol: String) = userProfile._id foreach (id => profileService.removeFavoriteSymbol(id, symbol))

  def addRecentSymbol(symbol: String) = userProfile._id foreach (id => profileService.addRecentSymbol(id, symbol))

  def getRecentSymbols = userProfile.recentSymbols

  def isRecentSymbol(symbol: String) = getRecentSymbols.contains(symbol)

  def removeRecentSymbol(symbol: String) = userProfile._id foreach (id => profileService.removeRecentSymbol(id, symbol))

  def getMostRecentSymbol = getRecentSymbols.lastOption getOrElse "AAPL"

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def getContest = contest getOrElse JS()

  def getContestID = contest.map(_._id).orNull

  def getContestName = contest.map(_.name).orNull

  def getContestStatus = contest.map(_.status).orNull

  def setContest(aContest: Contest) = {
    console.log(s"contest = ${angular.toJson(aContest)}")

    // if null or undefined, just reset the contest
    if (!isDefined(aContest)) resetContest()

    // if the contest contained an error, show it
    else if (isDefined(aContest.dynamic.error)) toaster.error(aContest.dynamic.error)

    // is it a delta?
    else if (aContest.dynamic.`type`.asOpt[String] ?== "delta") updateContestDelta(aContest)

    // otherwise, digest the full contest
    else contest = Option(aContest)
  }

  /**
    * Returns the combined total funds for both the cash and margin accounts
    */
  def getCompleteFundsAvailable = {
    (cashAccount_?.map(_.cashFunds) getOrElse 0.00d) + (marginAccount_?.map(_.cashFunds) getOrElse 0.00d)
  }

  def getFundsAvailable = cashAccount_?.map(_.cashFunds) getOrElse 0.00d

  def deductFundsAvailable(amount: Double) = {
    participant foreach { player =>
      console.log(s"Deducting funds: $amount from ${player.cashAccount.cashFunds}")
      player.cashAccount.cashFunds -= amount
      // TODO rethink this
    }
    ()
  }

  def getMessages = contest.flatMap(c => Option(c.messages)) getOrElse emptyArray[Message]

  def setMessages(messages: js.Array[Message]) = contest.foreach(_.messages = messages)

  def getMyAwards = userProfile.awards

  def getOrders = participant.map(_.orders) getOrElse emptyArray

  def getClosedOrders = participant.map(_.closedOrders) getOrElse emptyArray

  def getPerformance = participant.map(_.performance) getOrElse emptyArray

  def getPerks = participant.map(_.perks) getOrElse emptyArray

  def hasPerk(perkCode: String) = getPerks.contains(perkCode)

  def getPositions = participant.map(_.positions) getOrElse emptyArray

  def resetContest() = contest = None

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
    // load the user"s Facebook profile
      fbProfile <- {
        console.log(s"Retrieving Facebook profile for FBID $facebookID...")
        facebook.getUserProfile
      }

      friends <- {
        console.log(s"Loading Facebook friends for FBID $facebookID...")
        facebook.getTaggableFriends
      }

      fbCloseFriends <- {
        console.log(s"Loading Facebook friends list for FBID $facebookID...")
        facebook.getFriendList()
      }

      // load the user's ShockTrade profile
      profile <- {
        console.log(s"Retrieving ShockTrade profile for FBID $facebookID...")
        //profileService.getProfileByFacebookID(facebookID)
        Future.successful(userProfile)
      }
    } yield (fbProfile, friends, fbCloseFriends, profile)

    outcome onComplete {
      case Success((fbProfile, friends, fbCloseFriends, profile)) =>
        console.log("ShockTrade user profile, Facebook profile, and friends loaded...")
        console.log(s"fbCloseFriends = ${angular.toJson(fbCloseFriends, pretty = true)}")
        fbFriends = fbFriends
      case Failure(e) =>
        toaster.error(s"ShockTrade Profile retrieval error - ${e.getMessage}")
    }
    ()
  }

  def isFacebookConnected = facebookID.nonEmpty

  def initLinkedIn(IN: LinkedIn) = {
    // read the authenticated user's profile
    IN.API.Profile(js.Array("me")) onComplete {
      case Success(response) =>
        linkedInID = response.values.headOption.flatMap(_.id.toOption).orUndefined
        console.log(s"LinkedIn: memberID = $linkedInID")
      case Failure(e) =>
        console.error(s"LinkedIn-Profile: ${e.displayMessage}")
    }
  }

  def isLinkedInConnected = linkedInID.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Notification Methods
  ////////////////////////////////////////////////////////////

  def addNotification(message: String) = {
    if (notifications.push(message) > 20) notifications.shift()
    notifications
  }

  def getNotifications = notifications

  def hasNotifications = notifications.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Participant Methods
  ////////////////////////////////////////////////////////////

  def findPlayerByID(contest: Contest, playerId: BSONObjectID) = {
    if (isDefined(contest) && isDefined(contest.participants))
      contest.participants.find(_._id.exists(_.$oid.contains(playerId)))
    else
      None
  }

  def findPlayerByName(contest: Contest, playerName: String) = {
    contest.participants.find(_.name == playerName) getOrElse New
  }

  ////////////////////////////////////////////////////////////
  //          Private Methods
  ////////////////////////////////////////////////////////////

  /**
    * Creates a default 'Spectator' user profile
    * @return {{name: string, country: string, level: number, lastSymbol: string, friends: Array, filters: *[]}}
    */
  private def createSpectatorProfile() = {
    notifications.remove(0, notifications.length)
    facebookID = js.undefined
    fbFriends = emptyArray[TaggableFriend]
    fbProfile = js.undefined
    contest = None

    UserProfile(
      name = "Spectator",
      country = "us",
      lastSymbol = "AAPL"
    )
  }

  private[javascript] def cashAccount_? = participant.map(_.cashAccount)

  private[javascript] def marginAccount_? = participant.flatMap(_.marginAccount.toOption)

  private[javascript] def participant: Option[Participant] = {
    for {
      userId <- userProfile._id.toOption
      c <- contest
      me <- c.participants.find(p => BSONObjectID.isEqual(p._id, userId))
    } yield me
  }

  private def lookupParticipant(contest: Contest, playerId: String) = {
    contest.participants.find(_._id.exists(_.$oid.contains(playerId)))
  }

  ////////////////////////////////////////////////////////////
  //          Event Functions
  ////////////////////////////////////////////////////////////

  private def info(contest: Contest, message: String) = console.log(s"${contest.name}: $message")

  private def updateContestDelta(updatedContest: Contest) {
    // update the messages (if present)
    if (isDefined(updatedContest.messages)) {
      info(updatedContest, s"Updating messages")
      contest.foreach(_.messages = updatedContest.messages)
    }

    // lookup our local participant
    for {
      myParticipant <- participant
      myParticipantId <- myParticipant._id.map(_.$oid)
      foreignPlayer <- lookupParticipant(updatedContest, myParticipantId)
    } {
      // update each object (if present)
      if (isDefined(foreignPlayer.cashAccount)) updateContest_CashAccount(updatedContest, myParticipant, foreignPlayer)
      foreignPlayer.marginAccount.foreach(_ => updateContest_MarginAccount(updatedContest, myParticipant, foreignPlayer))
      if (isDefined(foreignPlayer.orders)) updateContest_ActiveOrders(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.closedOrders)) updateContest_ClosedOrders(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.performance)) updateContest_Performance(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.perks)) updateContest_Perks(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.positions)) updateContest_Positions(updatedContest, myParticipant, foreignPlayer)
    }
  }

  private def updateContest_CashAccount(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating cash account for ${foreignPlayer.name}")
    myParticipant.cashAccount = foreignPlayer.cashAccount
  }

  private def updateContest_MarginAccount(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating margin account for ${foreignPlayer.name}")
    myParticipant.marginAccount = foreignPlayer.marginAccount
  }

  private def updateContest_ActiveOrders(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating active orders for ${foreignPlayer.name}")
    myParticipant.orders = foreignPlayer.orders
  }

  private def updateContest_ClosedOrders(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating closed orders for ${foreignPlayer.name}")
    myParticipant.closedOrders = foreignPlayer.closedOrders
  }

  private def updateContest_Performance(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating performance for ${foreignPlayer.name}")
    myParticipant.performance = foreignPlayer.performance
  }

  private def updateContest_Perks(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating perks for ${foreignPlayer.name}")
    myParticipant.perks = foreignPlayer.perks
  }

  private def updateContest_Positions(contest: Contest, myParticipant: Participant, foreignPlayer: Participant) {
    info(contest, s"Updating positions for ${foreignPlayer.name}")
    myParticipant.positions = foreignPlayer.positions
  }

  $rootScope.$on(ContestDeleted, { (event: js.Dynamic, deletedContest: Contest) =>
    info(deletedContest, "Contest deleted event received...")
    for {
      contestId <- contest.flatMap(_._id.toOption)
      deletedId <- deletedContest._id.toOption
    } if (BSONObjectID.isEqual(contestId, deletedId)) resetContest()
  })

  $rootScope.$on(ContestUpdated, { (event: js.Dynamic, updatedContest: Contest) =>
    info(updatedContest, "Contest updated event received...")
    if (contest.isEmpty) setContest(updatedContest)
    else {
      for {
        contestId <- contest.flatMap(_._id.toOption)
        updatedId <- updatedContest._id.toOption
      } if (BSONObjectID.isEqual(contestId, updatedId)) setContest(updatedContest)
    }
  })

  $rootScope.$on(MessagesUpdated, { (event: js.Dynamic, updatedContest: Contest) =>
    info(updatedContest, "Messages updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(OrderUpdated, { (event: js.Dynamic, updatedContest: Contest) =>
    info(updatedContest, "Orders updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(PerksUpdated, { (event: js.Dynamic, updatedContest: Contest) =>
    info(updatedContest, "Perks updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(ParticipantUpdated, { (event: js.Dynamic, updatedContest: Contest) =>
    info(updatedContest, "Participant updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(UserProfileUpdated, { (event: js.Dynamic, profile: UserProfile) =>
    console.log(s"User Profile for ${profile.name} updated")
    for {
      userId <- userProfile._id
      otherId <- profile._id
    } if (BSONObjectID.isEqual(userId, otherId)) {
      userProfile.netWorth = profile.netWorth
      toaster.success("Your Wallet", s"<ul><li>Your wallet now has $$${profile.netWorth}</li></ul>", 5.seconds, "trustedHtml")
    }
  })

}
