package com.shocktrade.javascript

import com.ldaniels528.scalascript.angular
import com.ldaniels528.scalascript.ScalaJsHelper._
import com.ldaniels528.scalascript.core.{HttpPromise, Timeout}
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.{ScalaJsHelper, Scope, Service, named}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.profile.ProfileService

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Failure, Success}

/**
 * My Session Facade
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class MySession($rootScope: Scope, $timeout: Timeout, toaster: Toaster,
                @named("ContestService") contestService: ContestService,
                @named("ProfileService") profileService: ProfileService)
  extends Service {

  val notifications = emptyArray[String]
  var facebookID: Option[String] = None
  var fbFriends = emptyArray[js.Dynamic]
  var fbProfile: Option[js.Dynamic] = None
  var contest: Option[js.Dynamic] = None
  var userProfile: js.Dynamic = createSpectatorProfile()

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def getUserProfile: js.Function0[js.Dynamic] = () => userProfile

  def setUserProfile(profile: js.Dynamic, fbProfile: js.Dynamic, facebookID: String) {
    g.console.log(s"facebookID = $facebookID, fbProfile = ${angular.toJson(fbProfile)}")

    this.fbProfile = Some(fbProfile)
    this.facebookID = Some(facebookID)

    g.console.log(s"profile = ${angular.toJson(profile)}")
    this.userProfile = profile

    // broadcast the user profile change event
    $rootScope.$broadcast(UserProfileChanged, profile)
    ()
  }

  /**
   * Returns the user ID for the current user's ID
   * @return {*}
   */
  def getUserID: js.Function0[String] = () => userProfile.OID

  /**
   * Returns the user ID for the current user's name
   * @return {*}
   */
  def getUserName: js.Function0[String] = () => userProfile.name.as[String]

  /**
   * Indicates whether the given user is an administrator
   * @return {boolean}
   */
  def isAdmin: js.Function0[Boolean] = () => userProfile.admin.asOpt[Boolean].getOrElse(false)

  /**
   * Indicates whether the user is logged in
   * @return {boolean}
   */
  def isAuthenticated: js.Function0[Boolean] = () => userProfile.OID != null

  def getFacebookID: js.Function0[String] = () => facebookID.orNull

  def setFacebookID: js.Function1[String, Unit] = (fbId: String) => facebookID = Option(fbId)

  def getFacebookProfile: js.Function0[js.Dynamic] = () => fbProfile getOrElse JS()

  def setFacebookProfile: js.Function1[js.Dynamic, Unit] = (profile: js.Dynamic) => fbProfile = Option(profile)

  def isFbAuthenticated: js.Function0[Boolean] = () => fbProfile.isDefined

  /**
   * Logout function
   */
  def logout: js.Function0[Unit] = () => {
    facebookID = None
    fbFriends = js.Array[js.Dynamic]()
    fbProfile = None
    userProfile = createSpectatorProfile()
    contest = None
  }

  def refresh: js.Function0[Unit] = () => {
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

  def deduct: js.Function1[Double, js.Dynamic] = (amount: Double) => {
    g.console.log(f"Deducting $amount%.2f from ${userProfile.netWorth}")
    userProfile.netWorth -= amount
  }

  def getNetWorth: js.Function0[Double] = () => userProfile.netWorth.as[Double]

  /////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites, Recent, etc.
  /////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol: js.Function1[String, HttpPromise[js.Dynamic]] = (symbol: String) => {
    profileService.addFavoriteSymbol(getUserID(), symbol)
  }

  def getFavoriteSymbols: js.Function0[js.Array[String]] = () => userProfile.favorites.asArray[String]

  def isFavoriteSymbol: js.Function1[String, Boolean] = (symbol: String) => getFavoriteSymbols().contains(symbol)

  def removeFavoriteSymbol: js.Function1[String, HttpPromise[js.Dynamic]] = (symbol: String) => {
    profileService.removeFavoriteSymbol(getUserID(), symbol)
  }

  def addRecentSymbol: js.Function1[String, HttpPromise[js.Dynamic]] = (symbol: String) => {
    profileService.addRecentSymbol(getUserID(), symbol)
  }

  def getRecentSymbols: js.Function0[js.Array[String]] = () => userProfile.recentSymbols.asArray[String]

  def isRecentSymbol: js.Function1[String, Boolean] = (symbol: String) => getRecentSymbols().contains(symbol)

  def removeRecentSymbol: js.Function1[String, HttpPromise[js.Dynamic]] = (symbol: String) => {
    profileService.removeRecentSymbol(getUserID(), symbol)
  }

  def getMostRecentSymbol: js.Function0[String] = () => getRecentSymbols().lastOption getOrElse "AAPL"

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def contestIsEmpty: js.Function0[Boolean] = () => contest.isEmpty

  def getContest: js.Function0[js.Dynamic] = () => contest getOrElse JS()

  def getContestID: js.Function0[String] = () => contest.map(_.OID).orNull

  def getContestName: js.Function0[String] = () => contest.map(_.name).orNull.as[String]

  def getContestStatus: js.Function0[String] = () => contest.map(_.status).orNull.as[String]

  def setContest: js.Function1[js.Dynamic, Unit] = (aContest: js.Dynamic) => {
    // if null or undefined, just reset the contest
    if (!isDefined(aContest)) resetContest()

    // if the contest contained an error, show it
    else if (isDefined(aContest.error)) toaster.error(aContest.error)

    // is it a delta?
    else if (aContest.`type` === "delta") updateContestDelta(aContest)

    // otherwise, digest the full contest
    else contest = Option(aContest)
  }

  /**
   * Returns the combined total funds for both the cash and margin accounts
   */
  def getCompleteFundsAvailable: js.Function0[Double] = () => {
    (cashAccount_?.map(_.cashFunds.as[Double]) getOrElse 0.00d) + (marginAccount_?.map(_.cashFunds.as[Double]) getOrElse 0.00d)
  }

  def getFundsAvailable: js.Function0[Double] = () => cashAccount_?.flatMap(a => Option(a.cashFunds)).map(_.as[Double]) getOrElse 0.00d

  def deductFundsAvailable: js.Function1[Double, Unit] = (amount: Double) => {
    participant.foreach { player =>
      g.console.log("Deducting funds: " + amount + " from " + player.cashAccount.cashFunds)
      player.cashAccount.cashFunds -= amount
      // TODO rethink this
    }
    ()
  }

  def hasMarginAccount: js.Function0[Boolean] = () => marginAccount_?.isDefined

  def getCashAccount: js.Function0[js.Dynamic] = () => cashAccount_? getOrElse JS()

  def getMarginAccount: js.Function0[js.Dynamic] = () => marginAccount_? getOrElse JS()

  def setMessages: js.Function1[js.Array[js.Dynamic], Unit] = (messages: js.Array[js.Dynamic]) => {
    contest.foreach(_.messages = messages)
  }

  def getMessages: js.Function0[js.Array[js.Dynamic]] = () => {
    contest.flatMap(c => Option(c.messages).map(_.asArray[js.Dynamic])) getOrElse emptyArray[js.Dynamic]
  }

  def getMyAwards: js.Function0[js.Array[String]] = () => userProfile.awards.asArray[String]

  def getOrders: js.Function0[js.Array[js.Dynamic]] = () => {
    participant.flatMap(p => Option(p.orders).map(_.asArray[js.Dynamic])) getOrElse emptyArray[js.Dynamic]
  }

  def getClosedOrders: js.Function0[js.Array[js.Dynamic]] = () => {
    participant.flatMap(p => Option(p.closedOrders.asArray[js.Dynamic])) getOrElse emptyArray[js.Dynamic]
  }

  def participantIsEmpty: js.Function0[Boolean] = () => participant.isEmpty

  def getParticipant: js.Function0[js.Dynamic] = () => participant getOrElse JS()

  def getPerformance: js.Function0[js.Array[js.Dynamic]] = () => {
    participant.flatMap(p => Option(p.performance).map(_.asArray[js.Dynamic])) getOrElse emptyArray[js.Dynamic]
  }

  def getPerks: js.Function0[js.Array[String]] = () => {
    participant.flatMap(p => Option(p.perks).map(_.asArray[String])) getOrElse emptyArray[String]
  }

  def hasPerk: js.Function1[String, Boolean] = (perkCode: String) => getPerks().contains(perkCode)

  def getPositions: js.Function0[js.Array[js.Dynamic]] = () => {
    participant.flatMap(p => Option(p.positions).map(_.asArray[js.Dynamic])) getOrElse emptyArray[js.Dynamic]
  }

  def resetContest: js.Function0[Unit] = () => contest = None

  ////////////////////////////////////////////////////////////
  //          Notification Methods
  ////////////////////////////////////////////////////////////

  def addNotification: js.Function1[String, js.Array[String]] = (message: String) => {
    if (notifications.push(message) > 20) {
      notifications.shift()
    }
    notifications
  }

  def getNotifications: js.Function0[js.Array[String]] = () => notifications

  def hasNotifications: js.Function0[Boolean] = () => notifications.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Participant Methods
  ////////////////////////////////////////////////////////////

  def findPlayerByID: js.Function2[js.Dynamic, String, Option[js.Dynamic]] = (contest: js.Dynamic, playerId: String) => {
    if (isDefined(contest) && isDefined(contest.participants))
      contest.participants.asArray[js.Dynamic].find(_.OID == playerId)
    else
      None
  }

  def findPlayerByName: js.Function2[js.Dynamic, String, js.Dynamic] = (contest: js.Dynamic, playerName: String) => {
    required("contest", contest)
    required("playerName", playerName)
    contest.participants.asArray[js.Dynamic].find(_.name === playerName) getOrElse JS()
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
    facebookID = None
    fbFriends = emptyArray[js.Dynamic]
    fbProfile = None
    contest = None

    JS(
      name = "Spectator",
      country = "us",
      level = 1,
      lastSymbol = "AAPL",
      netWorth = 0.00,
      totalXP = 0,
      awards = emptyArray[String],
      favorites = emptyArray[String],
      recentSymbols = emptyArray[String]
    )
  }

  private[javascript] def cashAccount_? = participant.flatMap(p => Option(p.cashAccount))

  private[javascript] def marginAccount_? = participant.flatMap(p => Option(p.marginAccount))

  private[javascript] def participant: Option[js.Dynamic] = {
    var userId = userProfile.OID
    if (userId == null) None
    else for {
      c <- contest
      participants = if (isDefined(c.participants)) c.participants.asArray[js.Dynamic] else emptyArray[js.Dynamic]
      me = participants.find(_.OID == userProfile.OID) getOrElse JS()
    } yield me
  }

  private def lookupParticipant(contest: js.Dynamic, playerId: String) = {
    contest.participants.asArray[js.Dynamic].find(_.OID == playerId)
  }

  ////////////////////////////////////////////////////////////
  //          Event Functions
  ////////////////////////////////////////////////////////////

  private def info(contest: js.Dynamic, message: String) = g.console.log(s"${contest.name}: $message")

  private def updateContestDelta(updatedContest: js.Dynamic) {
    // update the messages (if present)
    if (isDefined(updatedContest.messages)) {
      info(updatedContest, s"Updating messages")
      contest.foreach(_.messages = updatedContest.messages)
    }

    // lookup our local participant
    for {
      myParticipant <- participant
      foreignPlayer <- lookupParticipant(updatedContest, myParticipant.OID)
    } {
      // update each object (if present)
      if (isDefined(foreignPlayer.cashAccount)) updateContest_CashAccount(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.marginAccount)) updateContest_MarginAccount(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.orders)) updateContest_ActiveOrders(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.closedOrders)) updateContest_ClosedOrders(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.performance)) updateContest_Performance(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.perks)) updateContest_Perks(updatedContest, myParticipant, foreignPlayer)
      if (isDefined(foreignPlayer.positions)) updateContest_Positions(updatedContest, myParticipant, foreignPlayer)
    }
  }

  private def updateContest_CashAccount(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating cash account for ${foreignPlayer.name}")
    myParticipant.cashAccount = foreignPlayer.cashAccount
  }

  private def updateContest_MarginAccount(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating margin account for ${foreignPlayer.name}")
    myParticipant.marginAccount = foreignPlayer.marginAccount
  }

  private def updateContest_ActiveOrders(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating active orders for ${foreignPlayer.name}")
    myParticipant.orders = foreignPlayer.orders
  }

  private def updateContest_ClosedOrders(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating closed orders for ${foreignPlayer.name}")
    myParticipant.closedOrders = foreignPlayer.closedOrders
  }

  private def updateContest_Performance(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating performance for ${foreignPlayer.name}")
    myParticipant.performance = foreignPlayer.performance
  }

  private def updateContest_Perks(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating perks for ${foreignPlayer.name}")
    myParticipant.perks = foreignPlayer.perks
  }

  private def updateContest_Positions(contest: js.Dynamic, myParticipant: js.Dynamic, foreignPlayer: js.Dynamic) {
    info(contest, s"Updating positions for ${foreignPlayer.name}")
    myParticipant.positions = foreignPlayer.positions
  }

  $rootScope.$on(ContestDeleted, { (event: js.Dynamic, deletedContest: js.Dynamic) =>
    info(deletedContest, "Contest deleted event received...")
    contest foreach { c =>
      if (c.OID == deletedContest.OID) resetContest()
    }
  })

  $rootScope.$on(ContestUpdated, { (event: js.Dynamic, updatedContest: js.Dynamic) =>
    info(updatedContest, "Contest updated event received...")
    contest foreach { c => if (c.OID == updatedContest.OID) setContest(updatedContest) }
    if (contest.isEmpty) setContest(updatedContest)
  })

  $rootScope.$on(MessagesUpdated, { (event: js.Dynamic, updatedContest: js.Dynamic) =>
    info(updatedContest, "Messages updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(OrderUpdated, { (event: js.Dynamic, updatedContest: js.Dynamic) =>
    info(updatedContest, "Orders updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(PerksUpdated, { (event: js.Dynamic, updatedContest: js.Dynamic) =>
    info(updatedContest, "Perks updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(ParticipantUpdated, { (event: js.Dynamic, updatedContest: js.Dynamic) =>
    info(updatedContest, "Participant updated event received...")
    updateContestDelta(updatedContest)
  })

  $rootScope.$on(UserProfileUpdated, { (event: js.Dynamic, profile: js.Dynamic) =>
    g.console.log(s"User Profile for ${profile.name} updated")
    if (userProfile.OID == profile.OID) {
      userProfile.netWorth = profile.netWorth
      toaster.success("Your Wallet", s"<ul><li>Your wallet now has $$${profile.netWorth}</li></ul>", 5.seconds, "trustedHtml")
    }
  })

}
