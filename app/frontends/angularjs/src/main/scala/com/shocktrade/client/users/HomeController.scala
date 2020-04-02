package com.shocktrade.client.users

import com.shocktrade.client.users.HomeController.HomeControllerScope
import com.shocktrade.client.{GlobalLoading, GlobalNavigation, RootScope}
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.social.facebook.TaggableFriend
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Home Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class HomeController($scope: HomeControllerScope, $timeout: Timeout, toaster: Toaster,
                     @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading {

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    //if (!mySession.isAuthenticated) $scope.switchToDiscover()
  }

  $scope.getAwards = () => $scope.userProfile.flatMap(_.awards) getOrElse emptyArray

  $scope.getFriends = () => js.Array()

  $scope.getNextLevelXP = () => $scope.userProfile.flatMap(_.nextLevelXP)

  $scope.getStars = () => (1 to $scope.userProfile.flatMap(_.totalXP.map(_ / 1000)).getOrElse(1)).toJSArray

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
  trait HomeControllerScope extends RootScope with GlobalNavigation {
    // variables
    //var userProfile: js.UndefOr[UserProfile] = js.native

    // functions
    var init: js.Function0[Unit] = js.native
    var getAwards: js.Function0[js.Array[String]] = js.native
    var getFriends: js.Function0[js.Array[TaggableFriend]] = js.native
    var getNextLevelXP: js.Function0[js.UndefOr[Int]] = js.native
    var getStars: js.Function0[js.Array[Int]] = js.native
    var getTotalXP: js.Function0[Int] = js.native
  }

}