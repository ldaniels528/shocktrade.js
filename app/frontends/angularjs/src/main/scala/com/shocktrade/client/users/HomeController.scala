package com.shocktrade.client.users

import com.shocktrade.client.users.HomeController.FacebookFriend
import com.shocktrade.client.{GlobalLoading, GlobalNavigation, RootScope}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.social.facebook.TaggableFriend
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr

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

  $scope.getStars = () => (1 to $scope.userProfile.flatMap(_.rep).getOrElse(3)).toJSArray

  $scope.getTotalXP = () => $scope.userProfile.flatMap(_.totalXP).getOrElse(0)

  $scope.selectFriend = (friendOpt: js.UndefOr[FacebookFriend]) => friendOpt foreach { friend =>
    console.log(s"selecting friend ${angular.toJson(friend)}")
    $scope.selectedFriend = friend
  }

}

/**
 * Home Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object HomeController {

  @js.native
  trait FacebookFriend extends TaggableFriend {
    var gamesCompleted: js.UndefOr[Int] = js.native
    var gamesCreated: js.UndefOr[Int] = js.native
    var gamesDeleted: js.UndefOr[Int] = js.native
    var lastLoginTime: js.UndefOr[js.Date] = js.native
    var status: js.UndefOr[String] = js.native
  }

}

/**
 * Home Controller Scope
 */
@js.native
trait HomeControllerScope extends RootScope with GlobalNavigation {
  // variables
  var selectedFriend: js.UndefOr[TaggableFriend] = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var getAwards: js.Function0[js.Array[String]] = js.native
  var getFriends: js.Function0[js.Array[TaggableFriend]] = js.native
  var getNextLevelXP: js.Function0[js.UndefOr[Int]] = js.native
  var getStars: js.Function0[js.Array[Int]] = js.native
  var getTotalXP: js.Function0[Int] = js.native
  var selectFriend: js.Function1[UndefOr[FacebookFriend], Unit] = js.native
}