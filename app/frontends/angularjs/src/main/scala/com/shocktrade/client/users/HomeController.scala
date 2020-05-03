package com.shocktrade.client.users

import com.shocktrade.client.contest.{AwardsSupport, AwardsSupportScope}
import com.shocktrade.client.users.HomeController.HomeControllerScope
import com.shocktrade.client.{GameStateService, GlobalLoading, GlobalNavigation, RootScope}
import com.shocktrade.common.forms.ContestSearchOptions
import com.shocktrade.common.forms.ContestSearchOptions.ContestStatus
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Home Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class HomeController($scope: HomeControllerScope, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                          @injected("GameStateService") gameStateService: GameStateService,
                          @injected("UserService") userService: UserService)
  extends Controller with AwardsSupport with GlobalLoading {

  $scope.statuses = ContestSearchOptions.contestStatuses

  $scope.myGamesSearchOptions = new ContestSearchOptions(
    userID = gameStateService.getUserID,
    buyIn = js.undefined,
    continuousTrading = false,
    duration = js.undefined,
    friendsOnly = false,
    invitationOnly = false,
    levelCap = js.undefined,
    levelCapAllowed = false,
    myGamesOnly = true,
    nameLike = js.undefined,
    perksAllowed = false,
    robotsAllowed = false,
    status = $scope.statuses.headOption.orUndefined
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.initHome = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    //if (!mySession.isAuthenticated) $scope.switchToDiscover()
  }

  $scope.getNextLevelXP = () => $scope.userProfile.flatMap(_.nextLevelXP)

  $scope.getStars = () => (1 to Math.max(1, $scope.userProfile.flatMap(_.totalXP.map(_ / 1000)).orZero)).toJSArray

  $scope.getTotalXP = () => $scope.userProfile.flatMap(_.totalXP).getOrElse(0)

}

/**
 * Home Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object HomeController {

  /**
   * Home Controller Scope
   */
  @js.native
  trait HomeControllerScope extends RootScope with AwardsSupportScope with GlobalNavigation {
    // variables
    var myGamesSearchOptions: ContestSearchOptions = js.native
    var statuses: js.Array[ContestStatus] = js.native
    //var userProfile: js.UndefOr[UserProfile] = js.native

    // functions
    var initHome: js.Function0[Unit] = js.native
    var getNextLevelXP: js.Function0[js.UndefOr[Int]] = js.native
    var getStars: js.Function0[js.Array[Int]] = js.native
    var getTotalXP: js.Function0[Int] = js.native
  }

}