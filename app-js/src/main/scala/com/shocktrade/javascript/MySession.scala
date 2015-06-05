package com.shocktrade.javascript

import com.greencatsoft.angularjs.core.{Log, RootScope}
import com.greencatsoft.angularjs.injectable
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.app.model.Contest.{Message, Participant}
import com.shocktrade.javascript.app.model.{Contest, FaceBookProfile, UserProfile}
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.profile.ProfileService
import prickle.Pickle

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.timers._
import scala.util.{Failure, Success}

/**
 * My Session Service
 * @author lawrence.daniels@gmail.com
 */
@JSExport
@injectable("MySession")
class MySession($rootScope: RootScope,
                $log: Log,
                toaster: Toaster,
                contestService: ContestService,
                profileService: ProfileService) extends ServiceSupport {

  var userProfile: Option[UserProfile] = None
  var authenticated = false
  var fbAuthenticated = false
  var fbUserID: Option[String] = None
  var fbFriends: List[String] = Nil
  var fbProfile: Option[FaceBookProfile] = None
  var contest: Option[Contest] = None
  var participant: Option[Participant] = None
  var totalInvestment: Option[Double] = None
  var totalInvestmentStatus: Option[String] = None

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  /**
   * Returns the user ID for the current user's ID
   * @return
   */
  def getUserID = userProfile.map(_.id)

  /**
   * Returns the user ID for the current user's name
   * @return
   */
  def getUserName = userProfile.map(_.name)

  /**
   * Indicates whether the given user is an administrator
   * @return {boolean}
   */
  def isAdmin = userProfile.exists(_.admin)

  /**
   * Indicates whether the user is logged in
   * @return {boolean}
   */
  def isAuthorized = userProfile.isDefined

  /**
   * Logout function
   */
  def logout() {
    authenticated = false
    fbAuthenticated = false
    fbUserID = None
    fbFriends = Nil
    fbProfile = None
    userProfile = Some(createSpectatorProfile())
  }

  def refresh() {
    for {
      myProfile <- userProfile
      fbId <- fbUserID
    } {
      profileService.getProfileByFacebookID(fbId).onComplete {
        case Success(profile) =>
          myProfile.netWorth = profile.netWorth
        case Failure(e) =>
          toaster.pop("info", e.getMessage, null)
      }
    }
  }

  /**
   * Creates a default 'Spectator' user profile
   * @return {{name: string, country: string, level: number, lastSymbol: string, friends: Array, filters: *[]}}
   */
  def createSpectatorProfile() = UserProfile(name = "Spectator")

  /////////////////////////////////////////////////////////////////////
  //          NetWorth Functions
  /////////////////////////////////////////////////////////////////////

  def deduct(amount: Double) {
    userProfile.foreach { u =>
      $log.info(s"Deducting $amount from ${u.netWorth}")
      u.netWorth -= amount
    }
  }

  def getNetWorth = userProfile.map(_.netWorth)

  def getTotalCashAvailable = userProfile.map(_.netWorth).getOrElse(0.00)

  def getTotalInvestment = {
    // lookup the player
    if (!isTotalInvestmentLoaded && totalInvestmentStatus.isEmpty) {
      totalInvestmentStatus = Some("LOADING")

      // load the total investment
      loadTotalInvestment()
    }

    totalInvestment
  }

  def isTotalInvestmentLoaded: Boolean = totalInvestment.isEmpty

  def reloadTotalInvestment() {
    def totalInvestmentStatus = null
  }

  def loadTotalInvestment() {
    // set a timeout so that loading doesn't persist
    setTimeout(20.seconds) {
      if (!isTotalInvestmentLoaded) {
        $log.error("Total investment call timed out")
        totalInvestmentStatus = Some("TIMEOUT")
      }
    }

    // retrieve the total investment
    userProfile.foreach { profile =>
      $log.info("Loading Total investment...")
      contestService.getTotalInvestment(profile.id).onComplete {
        case Success(netWorth) =>
          totalInvestment = Option(netWorth)
          totalInvestmentStatus = Some("LOADED")
          $log.info("Total investment loaded")

        case Failure(e) =>
          toaster.pop("error", "Error loading total investment", null)
          totalInvestmentStatus = Some("FAILED")
          $log.error("Total investment call failed")
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def contestIsEmpty = contest.isEmpty

  def getContest = contest

  def getContestID: Option[String] = contest.map(_.id)

  def getContestName = contest.map(_.name)

  def getContestStatus = contest.map(_.status)

  def setContest(contest_? : Option[Contest]) {
    contest_? match {
      case None => resetContest()
      case Some(c) =>
        // if the contest contained an error, show it
        if (c.error.isDefined) toaster.pop("error", c.error.get, null)

        // is it a delta?
        else if (c.`type` == "delta") updateContestDelta(c)

        // otherwise, digest the full contest
        else {
          contest = Some(c)
          participant = None
          // $rootScope.$emit("contest_selected", Pickle.intoString(c))
        }
    }
  }

  /**
   * Returns the combined total funds for both the cash and margin accounts
   */
  def getCompleteFundsAvailable = getFundsAvailable + getMarginCashAvailable

  def getFundsAvailable = participant.flatMap(_.cashAccount.map(_.cashFunds)).getOrElse(0.00d)

  def getMarginCashAvailable = participant.flatMap(_.marginAccount.map(_.cashFunds)).getOrElse(0.00d)

  def deductFundsAvailable(amount: Double) {
    participant.foreach { p =>
      $log.info(s"Deducting funds: $amount from ${p.cashAccount.map(_.cashFunds)}")
      p.cashAccount.foreach(_.cashFunds -= amount)
    }
  }

  def hasMarginAccount = participant.exists(_.marginAccount.isDefined)

  def getCashAccount = participant.flatMap(_.cashAccount)

  def getMarginAccount = participant.flatMap(_.marginAccount)

  def getMessages = contest.map(_.messages).getOrElse(Nil)

  def setMessages(messages: List[Message]) = contest.foreach(_.messages = messages)

  def getOrders = participant.flatMap(_.orders)

  def getClosedOrders = participant.flatMap(_.closedOrders)

  def participantIsEmpty = participant.isDefined

  def getParticipant: Option[Participant] = {
    if (participant.isEmpty) {
      for (c <- contest; u <- userProfile) {
        participant = c.participants.find(_.id == u.id)
      }
    }
    participant
  }

  def getPerformance = participant.flatMap(_.performance)

  def getPerks = participant.flatMap(_.perks)

  def hasPerk(perkCode: String) = participant.exists(_.perks.exists(_.contains(perkCode)))

  def getPositions = participant.flatMap(_.positions)

  def resetContest() {
    contest = None
    participant = None
  }

  ////////////////////////////////////////////////////////////
  //          Watch Events
  ////////////////////////////////////////////////////////////

  def updateContestDelta(contest: Contest) {
    // update the messages (if present)
    if (contest.messages.nonEmpty) {
      contest.messages = contest.messages
    }

    // lookup our local and updated participants
    for {
      currentPlayer <- participant
      updatedPlayer <- contest.participants.find(_.id == currentPlayer.id)
    } {
      // update cash account (if present)
      updatedPlayer.cashAccount.foreach { _ =>
        $log.info(s"${contest.name}: Updating cash account for ${updatedPlayer.name}")
        currentPlayer.cashAccount = updatedPlayer.cashAccount
      }

      // update the margin account (if present)
      updatedPlayer.marginAccount.foreach { _ =>
        $log.info(s"${contest.name}: Updating margin account for ${updatedPlayer.name}")
        currentPlayer.marginAccount = updatedPlayer.marginAccount
      }

      // update the orders (if present)
      updatedPlayer.orders.foreach { _ =>
        $log.info(s"${contest.name}: Updating orders for ${updatedPlayer.name}")
        currentPlayer.orders = updatedPlayer.orders
      }

      // update the order history (if present)
      updatedPlayer.closedOrders.foreach { _ =>
        $log.info(s"${contest.name}: Updating closed orders for ${updatedPlayer.name}")
        currentPlayer.closedOrders = updatedPlayer.closedOrders
      }

      // update the performance (if present)
      updatedPlayer.performance.foreach { _ =>
        $log.info(s"${contest.name}: Updating performance for ${updatedPlayer.name}")
        currentPlayer.performance = updatedPlayer.performance
      }

      // update the perks (if present)
      updatedPlayer.perks.foreach { _ =>
        $log.info(s"${contest.name}: Updating perks for ${updatedPlayer.name}")
        currentPlayer.perks = updatedPlayer.perks
      }

      // update the positions (if present)
      updatedPlayer.positions.foreach { _ =>
        $log.info(s"${contest.name}: Updating positions for ${updatedPlayer.name}")
        currentPlayer.positions = updatedPlayer.positions
      }
    }
  }

  $rootScope.$on("contest_deleted", { (event: js.Dynamic, contestId: String) =>
    $log.info("[MySession] Contest '" + contestId + "' deleted")
    contest.foreach(c => if (c.id == contestId) resetContest())
  })

  $rootScope.$on("contest_updated", { (event: js.Dynamic, updated: Contest) =>
    $log.info(s"[MySession] Contest '${updated.name}' updated")
    contest.foreach(c => if (c.id == updated.id) setContest(Option(updated)))
  })

  $rootScope.$on("messages_updated", { (event: js.Dynamic, updated: Contest) =>
    $log.info(s"[MySession] Messages for Contest '${updated.name}' updated")
    contest.foreach(c => if (c.id == updated.id) setContest(Option(updated)))
  })

  $rootScope.$on("orders_updated", { (event: js.Dynamic, updated: Contest) =>
    $log.info(s"[MySession] Orders for Contest '${updated.name}' updated")
    contest.foreach(c => if (c.id == updated.id) setContest(Option(updated)))
  })

  $rootScope.$on("perks_updated", { (event: js.Dynamic, updated: Contest) =>
    $log.info(s"[MySession] Perks for Contest '${updated.name}' updated")
    contest.foreach(c => if (c.id == updated.id) setContest(Option(updated)))
  })

  $rootScope.$on("participant_updated", { (event: js.Dynamic, updated: Contest) =>
    $log.info(s"[MySession] Participant for Contest '${updated.name}' updated")
    contest.foreach(c => if (c.id == updated.id) setContest(Option(updated)))
  })

  $rootScope.$on("profile_updated", { (event: js.Dynamic, profile: UserProfile) =>
    $log.info(s"[MySession] User Profile for ${profile.name} updated")
    userProfile.foreach(_.netWorth = profile.netWorth)
    toaster.pop("success", "Your Wallet ", s"""<ul><li>Your wallet now has $${profile.netWorth}</li ></ul>""", 5.seconds, "trustedHtml")
  })

  ////////////////////////////////////////////////////////////
  //          Initialization
  ////////////////////////////////////////////////////////////

  // make sure we're starting fresh
  resetContest()

  // initialize the values as a logged-out user
  logout()

}

