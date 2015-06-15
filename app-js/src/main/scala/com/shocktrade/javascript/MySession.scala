package com.shocktrade.javascript

import biz.enef.angulate.core.{HttpPromise, Timeout}
import biz.enef.angulate.{Scope, Service, angular, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.profile.ProfileService

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Try, Failure, Success}

/**
 * My Session Facade
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class MySession($rootScope: Scope, $timeout: Timeout, toaster: Toaster,
                @named("ContestService") contestService: ContestService,
                @named("ProfileService") profileService: ProfileService)
  extends Service {

  var facebookID: Option[String] = None
  var fbFriends = js.Array[js.Dynamic]()
  var fbProfile: Option[js.Dynamic] = None
  var userProfile: js.Dynamic = createSpectatorProfile()
  var contest: Option[js.Dynamic] = None
  var nonMember: Boolean = true

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def getUserProfile: js.Function0[js.Dynamic] = () => userProfile

  def setUserProfile: js.Function1[js.Dynamic, Unit] = (profile: js.Dynamic) => {
    userProfile = if (isDefined(profile)) profile else createSpectatorProfile()
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
  def getUserName: js.Function0[String] = () => if (isDefined(userProfile.name)) userProfile.name.as[String] else null

  /**
   * Indicates whether the given user is an administrator
   * @return {boolean}
   */
  def isAdmin: js.Function0[Boolean] = () => isDefined(userProfile.admin) && userProfile.admin.as[Boolean]

  /**
   * Indicates whether the user is logged in
   * @return {boolean}
   */
  def isAuthorized: js.Function0[Boolean] = () => userProfile.OID != null

  def isAuthenticated: js.Function0[Boolean] = () => userProfile.OID != null

  def getFacebookID: js.Function0[String] = () => facebookID.orNull

  def setFacebookID: js.Function1[String, Unit] = (fbId: String) => facebookID = Option(fbId)

  def getFacebookProfile: js.Function0[js.Dynamic] = () => fbProfile getOrElse JS()

  def setFacebookProfile: js.Function1[js.Dynamic, Unit] = (profile: js.Dynamic) => fbProfile = Option(profile)

  def isFbAuthenticated: js.Function0[Boolean] = () => fbProfile.isDefined

  /**
   * Logout private def
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
          toaster.error("Error loading user profile", null);
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
  //          Symbols - Favorite
  /////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol: js.Function1[String, HttpPromise[js.Array[String]]] = (symbol: String) => {
    profileService.addFavoriteSymbol(getUserID(), symbol) onSuccess { symbols =>
      userProfile.favorites = symbols
    }
  }

  def getFavoriteSymbols: js.Function0[js.Array[String]] = () => userProfile.favorites.asArray[String]

  def isFavoriteSymbol: js.Function1[String, Boolean] = (symbol: String) => getFavoriteSymbols().contains(symbol)

  def removeFavoriteSymbol: js.Function1[String, HttpPromise[js.Array[String]]] = (symbol: String) => {
    profileService.removeFavoriteSymbol(getUserID(), symbol) onSuccess { symbols =>
      userProfile.favorites = symbols
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Symbols - Recent
  /////////////////////////////////////////////////////////////////////

  def addRecentSymbol: js.Function1[String, HttpPromise[js.Array[String]]] = (symbol: String) => {
    profileService.addRecentSymbol(getUserID(), symbol) onSuccess { symbols =>
      userProfile.favorites = symbols
    }
  }

  def getRecentSymbols: js.Function0[js.Array[String]] = () => userProfile.favorites.asArray[String]

  def isRecentSymbol: js.Function1[String, Boolean] = (symbol: String) => getRecentSymbols().contains(symbol)

  def removeRecentSymbol: js.Function1[String, HttpPromise[js.Array[String]]] = (symbol: String) => {
    profileService.removeRecentSymbol(getUserID(), symbol) onSuccess { symbols =>
      userProfile.favorites = symbols
    }
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
    else if (isDefined(aContest.error)) toaster.error(aContest.error.as[String])

    // is it a delta?
    else if (aContest.`type` === "delta") updateContestDelta(aContest)

    // otherwise, digest the full contest
    else {
      g.console.log(s"contest = ${angular.toJson(aContest, pretty = true)}")
      Try(contest = Some(aContest)) match {
        case Success(_) =>
        case Failure(e) =>
          g.console.error("Failed to set contest")
          e.printStackTrace()
      }
    }
    ()
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
  private def createSpectatorProfile() = JS(
    name = "Spectator",
    country = "us",
    level = 1,
    lastSymbol = "AAPL",
    netWorth = 0.00,
    totalXP = 0,
    awards = emptyArray[String],
    favorites = emptyArray[String],
    friends = emptyArray[String],
    recentSymbols = emptyArray[String]
  )

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
  //          Watch Events
  ////////////////////////////////////////////////////////////

  private def info(contest: js.Dynamic, message: String) = g.console.log(s"${contest.name}: $message")

  private def updateContestDelta(contest: js.Dynamic) {
    // update the messages (if present)
    if (isDefined(contest.messages)) {
      contest.messages = contest.messages
    }

    // lookup our local participant
    for {
      myParticipant <- participant
      foreignPlayer <- lookupParticipant(contest, myParticipant.OID)
    } {
      // update the cash account (if present)
      if (isDefined(foreignPlayer.cashAccount)) {
        info(contest, s"Updating cash account for ${foreignPlayer.name}")
        myParticipant.cashAccount = foreignPlayer.cashAccount
      }

      // update the margin account (if present)
      if (isDefined(foreignPlayer.marginAccount)) {
        info(contest, s"Updating margin account for ${foreignPlayer.name}")
        myParticipant.marginAccount = foreignPlayer.marginAccount
      }

      // update the orders (if present)
      if (isDefined(foreignPlayer.orders)) {
        info(contest, s"Updating active orders for ${foreignPlayer.name}")
        myParticipant.orders = foreignPlayer.orders
      }

      // update the order history (if present)
      if (isDefined(foreignPlayer.closedOrders)) {
        info(contest, s"Updating closed orders for ${foreignPlayer.name}")
        myParticipant.closedOrders = foreignPlayer.closedOrders
      }

      // update the performance (if present)
      if (isDefined(foreignPlayer.performance)) {
        info(contest, s"Updating performance for ${foreignPlayer.name}")
        myParticipant.performance = foreignPlayer.performance
      }

      // update the perks (if present)
      if (isDefined(foreignPlayer.perks)) {
        info(contest, s"Updating perks for ${foreignPlayer.name}")
        myParticipant.perks = foreignPlayer.perks
      }

      // update the positions (if present)
      if (isDefined(foreignPlayer.positions)) {
        info(contest, s"Updating positions for ${foreignPlayer.name}")
        myParticipant.positions = foreignPlayer.positions
      }
    }
  }

  $rootScope.$on("contest_deleted", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Contest deleted")
    this.contest foreach { c =>
      if (c.OID == contest.OID) resetContest()
    }
  })

  $rootScope.$on("contest_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Contest updated")
    this.contest foreach { c => if (c.OID == contest.OID) setContest(contest) }
    if (this.contest.isEmpty) setContest(contest)
  })

  $rootScope.$on("messages_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Messages updated")
    setContest(contest)
  })

  $rootScope.$on("orders_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Orders updated")
    setContest(contest)
  })

  $rootScope.$on("perks_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Perks updated")
    setContest(contest)
  })

  $rootScope.$on("participant_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Participant updated")
    setContest(contest)
  })

  $rootScope.$on("profile_updated", { (event: js.Dynamic, profile: js.Dynamic) =>
    g.console.log(s"User Profile for ${profile.name} updated")
    if (userProfile.OID == profile.OID) {
      userProfile.netWorth = profile.netWorth
      toaster.success("Your Wallet", s"<ul><li>Your wallet now has $$${profile.netWorth}</li></ul>", 5.seconds, "trustedHtml")
    }
  })

}
