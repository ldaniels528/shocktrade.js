package com.shocktrade.client

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ChatService, ContestService, PortfolioService}
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest._
import com.shocktrade.client.users.UserService
import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.facebook.FacebookService
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

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
                            @injected("UserService") profileService: UserService,
                            @injected("QuoteCache") quoteCache: QuoteCache)
  extends Service {

  private val notifications = emptyArray[String]

  var contest_? : Option[Contest] = None
  var portfolio_? : Option[Portfolio] = None
  var participant_? : Option[Participant] = None
  var userProfile: UserProfile = createSpectatorProfile()

  /////////////////////////////////////////////////////////////////////
  //          Authentication & Authorization Functions
  /////////////////////////////////////////////////////////////////////

  def setUserProfile(profile: UserProfile) {
    this.userProfile = profile

    // broadcast the user profile change event
    $rootScope.emitUserProfileChanged(profile)
    ()
  }

  /**
   * Returns the user ID for the current user's name
   * @return {*}
   */
  def getUserName: js.UndefOr[String] = userProfile.username

  /**
   * Indicates whether the given user is an administrator
   * @return {boolean}
   */
  def isAdmin: Boolean = userProfile.isAdmin.isTrue

  /**
   * Indicates whether the user is logged in
   * @return {boolean}
   */
  def isAuthenticated: Boolean = userProfile.userID.isAssigned

  /**
   * Logout function
   */
  def logout(): Unit = {
    userProfile = createSpectatorProfile()
    resetContest()
  }

  def resetContest(): Unit = {
    contest_? = None
    participant_? = None
    portfolio_? = None
  }

  /////////////////////////////////////////////////////////////////////
  //          NetWorth Functions
  /////////////////////////////////////////////////////////////////////

  /**
   * Creates a default 'Spectator' user profile
   * @return {{name: string, country: string, level: number, lastSymbol: string, friends: Array, filters: *[]}}
   */
  private def createSpectatorProfile() = {
    notifications.removeAll()
    resetContest()

    new UserProfile(
      username = "Spectator",
      country = "us",
      lastSymbol = "AAPL"
    )
  }

  /////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites, Recent, etc.
  /////////////////////////////////////////////////////////////////////

  def refresh(): Unit = {
    userProfile.userID foreach { userID =>

    }
    /*
    profileService.getProfileByFacebookID(userProfile._id.orNull) onComplete {
      case Success(response) =>
        $rootScope.$apply { () =>
          userProfile.netWorth = response.data.netWorth
          userProfile.wallet = response.data.wallet
        }
      case Failure(e) =>
        toaster.error("Error loading user profile");
    }*/
  }

  def deduct(amount: Double): Unit = userProfile.wallet = userProfile.wallet.map(_ - amount)

  def addFavoriteSymbol(symbol: String): Unit = userProfile.userID foreach (id => profileService.addFavoriteSymbol(id, symbol))

  def isFavoriteSymbol(symbol: String): Boolean = getFavoriteSymbols.exists(_.contains(symbol))

  def getFavoriteSymbols: js.UndefOr[js.Array[String]] = userProfile.favoriteSymbols

  def removeFavoriteSymbol(symbol: String): Unit = userProfile.userID foreach (id => profileService.removeFavoriteSymbol(id, symbol))

  def addRecentSymbol(symbol: String): Unit = userProfile.userID foreach (id => profileService.addRecentSymbol(id, symbol))

  def isRecentSymbol(symbol: String): Boolean = getRecentSymbols.exists(_.contains(symbol))

  def removeRecentSymbol(symbol: String): Unit = userProfile.userID foreach (id => profileService.removeRecentSymbol(id, symbol))

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def getMostRecentSymbol: String = getRecentSymbols.toOption.flatMap(_.lastOption) getOrElse "AAPL"

  def getRecentSymbols: js.UndefOr[js.Array[String]] = userProfile.recentSymbols

  def getContest: js.Object = contest_? getOrElse JS()

  def setContest(contest: Contest): Unit = {
    contest_? = Option(contest)

    // if the player is defined ...
    for {
      contestId <- contest.contestID
      portfolioID <- userProfile.userID
    } {
      // attempt to find the participant
      participant_? = for {
        portfolioID <- userProfile.userID.toOption
        participant <- contest.participants.toOption.flatMap(_.find(_.is(portfolioID)))
      } yield participant

      // lookup the portfolio
      portfolioService.getPortfolioByPlayer(portfolioID).map(_.data) foreach { portfolio =>
        portfolio_? = Option(portfolio)
      }
    }

    console.log(s"setContest: contest = %s, participant = %s, portfolio = %s",
      angular.toJson(contest), angular.toJson(participant_?.orNull), angular.toJson(portfolio_?.orNull))
  }

  def getContestID: js.UndefOr[String] = contest_?.orUndefined.flatMap(_.contestID)

  def getContestName: String = contest_?.flatMap(_.name.toOption).orNull

  def getContestStatus: String = contest_?.flatMap(_.status.toOption).orNull

  def loadContestByID(contestId: String)(implicit ec: ExecutionContext): Future[Option[(Contest, Portfolio, String, Option[Participant])]] = {
    Future.sequence {
      userProfile.userID map { portfolioID =>
        console.log(s"Loading contest $contestId (player: $portfolioID)...")
        for {
          contest <- contestService.findContestByID(contestId).map(_.data)
          portfolio <- portfolioService.getPortfolioByPlayer(portfolioID).map(_.data)
          participantOpt = contest.participants.toOption.flatMap(_.find(_.is(portfolioID)))
        } yield {
          // set the variables
          contest_? = Option(contest)
          participant_? = participantOpt
          portfolio_? = Option(portfolio)

          // broadcast the event
          $rootScope.emitContestSelected(contest)

          // return the tuple
          (contest, portfolio, portfolioID, participantOpt)
        }
      } toList
    } map (_.headOption)
  }

  def updateRankings(contest: Contest): Contest = {
    if (contest.leader.nonAssigned || contest.player.nonAssigned) {
      contest.leader = contest.participants.flatMap(_.find(_.rank.contains("1st")).orUndefined)
      contest.player = contest.participants.flatMap(_.find(_._id ?== userProfile.userID).orUndefined)
    }
    contest
  }

  def updatePortfolio(portfolio: Portfolio): Unit = {
    console.log(s"portfolio = ${angular.toJson(portfolio)}")
    portfolio_? = Option(portfolio)
  }

  def getMessages: js.Array[ChatMessage] = {
    contest_?.orUndefined.flatMap(_.messages.flat) getOrElse emptyArray[ChatMessage]
  }

  def setMessages(messages: js.Array[ChatMessage]): Unit = contest_?.foreach(_.messages = messages)

  def getMyAwards: js.Array[String] = userProfile.awards getOrElse emptyArray

  def getOrders: js.Array[Order] = portfolio_?.orUndefined.flatMap(_.orders) getOrElse emptyArray

  def getClosedOrders: js.Array[Order] = portfolio_?.orUndefined.flatMap(_.closedOrders) getOrElse emptyArray

  def getPerformance: js.Array[Performance] = portfolio_?.orUndefined.flatMap(_.performance) getOrElse emptyArray

  def hasPerk(perkCode: String): Boolean = getPerks.contains(perkCode)

  def getPerks: js.Array[String] = portfolio_?.orUndefined.flatMap(_.perks) getOrElse emptyArray

  ////////////////////////////////////////////////////////////
  //          Social Network Methods
  ////////////////////////////////////////////////////////////

  def getPositions: js.Array[Position] = portfolio_?.orUndefined.flatMap(_.positions) getOrElse emptyArray

  def doFacebookLogin(): Unit = {
    // perform the login
    console.log(s"Performing Facebook login...")
    facebook.login() onComplete {
      case Success(response) =>
        console.log("Facebook login successful.")
        doPostLoginUpdates(facebookID = response.authResponse.userID, userInitiated = false)
      case Failure(e) =>
        console.error(s"Facebook: ${e.displayMessage}")
    }
  }

  ////////////////////////////////////////////////////////////
  //          Notification Methods
  ////////////////////////////////////////////////////////////

  def doPostLoginUpdates(facebookID: String, userInitiated: Boolean): Unit = {
    /*
    console.log(s"userInitiated = $userInitiated")
    val outcome = for {
      // load the user"s ShockTrade profile
      profile <- {
        console.log(s"Retrieving ShockTrade profile for FBID $facebookID...")
        profileService.getProfileByFacebookID(facebookID).map(_.data)
      }

      // retrieve the updated network
      netWorth <- profileService.getNetWorth(profile._id.orNull).map(_.data)

    } yield (profile, netWorth)

    outcome onComplete {
      case Success((profile, netWorth)) =>
        console.log("ShockTrade user profile, Facebook profile, and friends loaded...")
        $rootScope.$apply(() => {
          profile.netWorth = netWorth.value
          setUserProfile(profile)
        })
      case Failure(e) =>
        toaster.error(s"ShockTrade Profile retrieval error - ${e.getMessage}")
    }*/
  }

  def addNotification(message: String): js.Array[String] = {
    while (notifications.push(message) > 20) notifications.shift()
    notifications
  }

  def getNotifications: js.Array[String] = notifications

  ////////////////////////////////////////////////////////////
  //          Participant Methods
  ////////////////////////////////////////////////////////////

  def hasNotifications: Boolean = notifications.nonEmpty

  ////////////////////////////////////////////////////////////
  //          Private Methods
  ////////////////////////////////////////////////////////////

  def findPlayerByID(contest: Contest, portfolioID: String): Option[Participant] = {
    if (isDefined(contest) && isDefined(contest.participants))
      contest.participants.toOption.flatMap(_.find(_._id.contains(portfolioID)))
    else
      None
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Events
  /////////////////////////////////////////////////////////////////////////////

  $rootScope.onUserProfileUpdated { (_, profile) =>
    console.log(s"User Profile for ${profile.username} updated")
    for {
      userId <- userProfile.userID
      otherId <- profile.userID
    } if (userId == otherId) {
      userProfile.wallet = profile.wallet
      toaster.success("Your Wallet", s"<ul><li>Your wallet now has $$${profile.wallet}</li></ul>", 5.seconds /*, "trustedHtml"*/)
    }
  }

}
