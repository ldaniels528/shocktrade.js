package com.shocktrade.javascript

import biz.enef.angulate.core.Timeout
import biz.enef.angulate.{Scope, Service, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.profile.ProfileService

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{Failure, Success}

/**
 * My Session Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class MySession($rootScope: Scope, $timeout: Timeout, toaster: Toaster,
                @named("ContestService") contestService: ContestService,
                @named("ProfileService") profileService: ProfileService) extends Service {
  var facebookID: Option[String] = None
  var fbFriends = js.Array[js.Dynamic]()
  var fbProfile: Option[js.Dynamic] = None
  var userProfile: js.Dynamic = createSpectatorProfile()
  var contest: Option[js.Dynamic] = None
  var nonMember: Boolean = true

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def getUserProfile: js.Function = () => userProfile

  def setUserProfile: js.Function = (profile: js.Dynamic) => userProfile = if (isDefined(profile)) profile else createSpectatorProfile()

  /**
   * Returns the user ID for the current user's ID
   * @return {*}
   */
  def getUserID: js.Function = () => getUserID_@

  def getUserID_@ : String = userProfile.OID

  /**
   * Returns the user ID for the current user's name
   * @return {*}
   */
  def getUserName: js.Function = () => userProfile.name

  def getUserName_@ = if (isDefined(userProfile.name)) userProfile.name.as[String] else null

  /**
   * Indicates whether the given user is an administrator
   * @return {boolean}
   */
  def isAdmin: js.Function = () => isDefined(userProfile.admin) && userProfile.admin.as[Boolean]

  /**
   * Indicates whether the user is logged in
   * @return {boolean}
   */
  def isAuthorized: js.Function = () => userProfile.OID != null

  def isAuthenticated: js.Function = () => userProfile.OID != null

  protected[javascript] def isAuthenticated_@ = userProfile.OID != null

  def getFacebookID: js.Function = () => facebookID.orNull

  def setFacebookID: js.Function = (fbId: String) => facebookID = Option(fbId)

  def getFacebookProfile: js.Function = () => fbProfile getOrElse JS()

  def setFacebookProfile: js.Function = (profile: js.Dynamic) => fbProfile = Option(profile)

  def isFbAuthenticated: js.Function = () => fbProfile.isDefined

  def facebookID_@ : String = facebookID.orNull

  /**
   * Logout private def
   */
  def logout = () => {
    facebookID = None
    fbFriends = js.Array[js.Dynamic]()
    fbProfile = None
    userProfile = createSpectatorProfile()
    contest = None
  }

  def refresh = () => {
    facebookID.foreach { fbId =>
      profileService.getProfileByFacebookID_@(fbId) onComplete {
        case Success(profile) =>
          userProfile.netWorth = profile.netWorth
        case Failure(e) =>
          toaster.pop("error", "Error loading user profile", null);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          NetWorth Functions
  /////////////////////////////////////////////////////////////////////

  def deduct: js.Function = (amount: Double) => {
    g.console.log(f"Deducting $amount%.2f from ${userProfile.netWorth}")
    userProfile.netWorth -= amount
  }

  def getNetWorth: js.Function = () => userProfile.netWorth

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def contestIsEmpty: js.Function = () => contest.isEmpty

  def getContest: js.Function = () => contest getOrElse JS()

  def getContestID: js.Function = () => contest.map(_.OID).orNull

  def getContestName: js.Function = () => contest.map(_.name).orNull

  def getContestStatus: js.Function = () => contest.map(_.status).orNull

  def setContest: js.Function = (contest: js.Dynamic) => setContest_@(contest)

  protected[javascript] def setContest_@(aContest: js.Dynamic) = {
    // if null or undefined, just reset the contest
    if (!isDefined(aContest)) resetContest_@()

    // if the contest contained an error, show it
    else if (isDefined(aContest.error)) toaster.pop("error", aContest.error.as[String], null)

    // is it a delta?
    else if (aContest.`type` === "delta") updateContestDelta(aContest)

    // otherwise, digest the full contest
    else {
      this.contest = Some(aContest)
      $rootScope.$emit("contest_selected", aContest)
    }
  }

  /**
   * Returns the combined total funds for both the cash and margin accounts
   */
  def getCompleteFundsAvailable: js.Function = () => {
    (cashAccount_?.map(_.cashFunds.as[Double]) getOrElse 0.00) + (marginAccount_?.map(_.cashFunds.as[Double]) getOrElse 0.00)
  }

  def getFundsAvailable: js.Function = () => cashAccount_?.map(_.cashFunds) getOrElse 0.00

  def deductFundsAvailable: js.Function = (amount: Double) => {
    participant.foreach { player =>
      g.console.log("Deducting funds: " + amount + " from " + player.cashAccount.cashFunds)
      player.cashAccount.cashFunds -= amount
    }
  }

  def hasMarginAccount: js.Function = () => marginAccount_?.isDefined

  def getCashAccount: js.Function = () => cashAccount_? getOrElse JS()

  def getMarginAccount: js.Function = () => marginAccount_? getOrElse JS()

  def setMessages: js.Function = (messages: js.Array[js.Dynamic]) => contest.foreach(_.messages = messages)

  def getMessages: js.Function = () => contest.map(_.messages) getOrElse emptyArray[js.Dynamic]

  def getOrders: js.Function = () => participant.flatMap(p => Option(p.orders)) getOrElse emptyArray[js.Dynamic]

  def getClosedOrders: js.Function = () => participant.flatMap(p => Option(p.closedOrders)) getOrElse emptyArray[js.Dynamic]

  def participantIsEmpty: js.Function = () => participant.isEmpty

  def getParticipant: js.Function = () => participant getOrElse JS()

  def getPerformance: js.Function = () => participant.flatMap(p => Option(p.performance)) getOrElse emptyArray[js.Dynamic]

  def getPerks: js.Function = () => getPerks_@

  def getPerks_@ = participant.flatMap(p => Option(p.perks).map(_.asArray[String])) getOrElse emptyArray[String]

  def hasPerk: js.Function = (perkCode: String) => getPerks_@.contains(perkCode)

  def getPositions: js.Function = () => participant.flatMap(p => Option(p.positions)) getOrElse emptyArray[js.Dynamic]

  def resetContest: js.Function = () => resetContest_@()

  def resetContest_@() = contest = None

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
      if (c.OID == contest.OID) resetContest_@()
    }
  })

  $rootScope.$on("contest_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Contest updated")
    this.contest foreach { c => if (c.OID == contest.OID) setContest_@(contest)}
    if (this.contest.isEmpty) setContest_@(contest)
  })

  $rootScope.$on("messages_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Messages updated")
    setContest_@(contest)
  })

  $rootScope.$on("orders_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Orders updated")
    setContest_@(contest)
  })

  $rootScope.$on("perks_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Perks updated")
    setContest_@(contest)
  })

  $rootScope.$on("participant_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    info(contest, "Participant updated")
    setContest_@(contest)
  })

  $rootScope.$on("profile_updated", { (event: js.Dynamic, profile: js.Dynamic) =>
    g.console.log(s"User Profile for ${profile.name} updated")
    if (userProfile.OID == profile.OID) {
      userProfile.netWorth = profile.netWorth
      toaster.pop("success", "Your Wallet", s"<ul><li>Your wallet now has $$${profile.netWorth}</li></ul>", 5000, "trustedHtml")
    }
  })

}
