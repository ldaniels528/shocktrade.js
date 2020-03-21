package com.shocktrade.client.users

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.client.{ContestFactory, RootScope}
import com.shocktrade.common.models.user.NetWorth
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Factory, injected}
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

  def contest: js.UndefOr[Contest] = $rootScope.contest

  def contest_=(contest: js.UndefOr[Contest]): Unit = {
    $rootScope.contest = contest
    contest.foreach($rootScope.emitContestSelected)
  }

  def netWorth: js.UndefOr[NetWorth] = $rootScope.netWorth

  def netWorth_=(netWorth: js.UndefOr[NetWorth]): Unit = {
    $rootScope.netWorth = netWorth
    // netWorth.foreach($rootScope.emitNetWorthChanged)
  }

  def portfolio: js.UndefOr[Portfolio] = $rootScope.portfolio

  def portfolio_=(portfolio: js.UndefOr[Portfolio]): Unit = {
    $rootScope.portfolio = portfolio
    // portfolio.foreach($rootScope.emitPortfolioChanged)
  }

  def userID: js.UndefOr[String] = userProfile.flatMap(_.userID)

  def userProfile: js.UndefOr[UserProfile] = $rootScope.userProfile

  def userProfile_=(userProfile: js.UndefOr[UserProfile]): Unit = {
    $rootScope.userProfile = userProfile
    userProfile.foreach($rootScope.emitUserProfileUpdated)
    refreshNetWorth()
    ()
  }

  def refreshContest(): GameStateFactory = {
    $rootScope.contest.flatMap(_.contestID) foreach { contestID =>
      console.info(s"Loading contest for user $contestID...")
      contestFactory.findContest(contestID) foreach { response => this.contest = response }
    }
    this
  }

  def refreshNetWorth(): GameStateFactory = {
    $rootScope.userProfile.flatMap(_.userID) foreach { userID =>
      console.info(s"Loading net-worth for user $userID...")
      userService.getNetWorth(userID) foreach { response => this.netWorth = response.data }
    }
    this
  }

  def refreshPortfolio(): GameStateFactory = {
    for (userID <- $rootScope.userProfile.flatMap(_.userID); contestID <- $rootScope.contest.flatMap(_.contestID)) {
      console.info(s"Loading portfolio for contest $contestID, user $userID...")
      contestFactory.findPortfolio(contestID, userID) foreach { response => this.portfolio = response }
    }
    this
  }

  def refreshUserProfile(): GameStateFactory = {
    $rootScope.userProfile.flatMap(_.userID) foreach { userID =>
      console.info(s"Loading profile for user $userID...")
      userService.getUserByID(userID) foreach { response => this.userProfile = response.data }
    }
    this
  }

  def reset(): Unit = {
    //$rootScope.contest = js.undefined
    $rootScope.netWorth = js.undefined
    $rootScope.portfolio = js.undefined
    $rootScope.userProfile = js.undefined
  }

}
