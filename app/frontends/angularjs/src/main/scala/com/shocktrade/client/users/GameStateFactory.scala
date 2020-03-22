package com.shocktrade.client.users

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.client.users.GameStateFactory.{ContestScope, NetWorthScope, PortfolioScope, UserProfileScope}
import com.shocktrade.client.{ContestFactory, RootScope}
import com.shocktrade.common.models.user.NetWorth
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Factory, Scope, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
 * Represents the state of a user session
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class GameStateFactory($rootScope: RootScope,
                       @injected("ContestFactory") contestFactory: ContestFactory,
                       @injected("UserService") userService: UserService) extends Factory {

  /////////////////////////////////////////////////////////////////////
  //          Contest Functions
  /////////////////////////////////////////////////////////////////////

  def contest(implicit $scope: ContestScope): js.UndefOr[Contest] = $scope.contest

  def contest_=(contest: js.UndefOr[Contest])(implicit $scope: ContestScope): Unit = {
    $scope.contest = contest
    contest.foreach($rootScope.emitContestSelected)
  }

  def refreshContest()(implicit $scope: ContestScope): GameStateFactory = {
    $scope.contest.flatMap(_.contestID) foreach { contestID =>
      console.info(s"Loading contest for user $contestID...")
      contestFactory.findContest(contestID) foreach { response => this.contest = response }
    }
    this
  }

  /////////////////////////////////////////////////////////////////////
  //          NetWorth Functions
  /////////////////////////////////////////////////////////////////////

  def netWorth(implicit $scope: NetWorthScope): js.UndefOr[NetWorth] = $scope.netWorth

  def netWorth_=(netWorth: js.UndefOr[NetWorth])(implicit $scope: NetWorthScope): Unit = {
    $scope.netWorth = netWorth
    // netWorth.foreach($rootScope.emitNetWorthChanged)
  }

  def refreshNetWorth()(implicit $scope: NetWorthScope with UserProfileScope): GameStateFactory = {
    $scope.userProfile.flatMap(_.userID) foreach { userID =>
      console.info(s"Loading net-worth for user $userID...")
      userService.getNetWorth(userID) foreach { response => this.netWorth = response.data }
    }
    this
  }

  /////////////////////////////////////////////////////////////////////
  //          Portfolio Functions
  /////////////////////////////////////////////////////////////////////

  def portfolio(implicit $scope: PortfolioScope): js.UndefOr[Portfolio] = $scope.portfolio

  def portfolio_=(portfolio: js.UndefOr[Portfolio])(implicit $scope: PortfolioScope): Unit = {
    $scope.portfolio = portfolio
    // portfolio.foreach($rootScope.emitPortfolioChanged)
  }

  def refreshPortfolio()(implicit $scope: PortfolioScope with UserProfileScope): GameStateFactory = {
    for (userID <- $scope.userProfile.flatMap(_.userID); contestID <- $scope.portfolio.flatMap(_.contestID)) {
      console.info(s"Loading portfolio for contest $contestID, user $userID...")
      contestFactory.findPortfolio(contestID, userID) foreach { response => this.portfolio = response }
    }
    this
  }

  /////////////////////////////////////////////////////////////////////
  //          User Profile Functions
  /////////////////////////////////////////////////////////////////////

  def userID(implicit $scope: UserProfileScope): js.UndefOr[String] = userProfile.flatMap(_.userID)

  def username(implicit $scope: UserProfileScope): js.UndefOr[String] = userProfile.flatMap(_.username)

  def userProfile(implicit $scope: UserProfileScope): js.UndefOr[UserProfile] = $scope.userProfile

  def userProfile_=(userProfile: js.UndefOr[UserProfile])(implicit $scope: UserProfileScope): Unit = {
    $scope.userProfile = userProfile
    userProfile.foreach($rootScope.emitUserProfileUpdated)
    ()
  }

  def refreshUserProfile()(implicit $scope: UserProfileScope): GameStateFactory = {
    $scope.userProfile.flatMap(_.userID) foreach { userID =>
      console.info(s"Loading profile for user $userID...")
      userService.findUserByID(userID) foreach { response => this.userProfile = response.data }
    }
    this
  }

  /////////////////////////////////////////////////////////////////////
  //          General Functions
  /////////////////////////////////////////////////////////////////////

  def reset()(implicit $scope: NetWorthScope with UserProfileScope): Unit = {
    $scope.netWorth = js.undefined
    $scope.userProfile = js.undefined
  }

}

/**
 * Game State Factory
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object GameStateFactory {

  @js.native
  trait ContestScope extends Scope {
    var contest: js.UndefOr[Contest] = js.native
  }

  @js.native
  trait NetWorthScope extends Scope {
    var netWorth: js.UndefOr[NetWorth] = js.native
  }

  @js.native
  trait PortfolioScope extends Scope {
    var portfolio: js.UndefOr[Portfolio] = js.native
  }

  @js.native
  trait UserProfileScope extends Scope {
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

}