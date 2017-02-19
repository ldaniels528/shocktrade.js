package com.shocktrade.client.profile

import com.shocktrade.client.profile.HomeController.FacebookFriend
import com.shocktrade.client.{GlobalLoading, GlobalNavigation, MySessionService}
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.dom.html.browser.console
import io.scalajs.social.facebook.TaggableFriend
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success}

/**
  * Home Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class HomeController($scope: HomeControllerScope, $timeout: Timeout, toaster: Toaster,
                     @injected("MySessionService") mySession: MySessionService,
                     @injected("UserService") userService: UserService,
                     @injected("UserProfileService") profileService: UserProfileService)
  extends Controller with GlobalLoading {

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    if (!mySession.isAuthenticated) $scope.switchToDiscover()
  }

  $scope.getAwards = () => mySession.userProfile.awards getOrElse emptyArray

  $scope.getFriends = () => mySession.fbFriends_?

  $scope.getNextLevelXP = () => mySession.userProfile.nextLevelXP

  $scope.getStars = () => (1 to mySession.userProfile.rep.getOrElse(3)).toJSArray

  $scope.getTotalXP = () => mySession.userProfile.totalXP.getOrElse(0)

  $scope.selectFriend = (friendOpt: js.UndefOr[FacebookFriend]) => friendOpt foreach { friend =>
    console.log(s"selecting friend ${angular.toJson(friend)}")
    $scope.selectedFriend = friend

    asyncLoading($scope)(userService.getFacebookFriendStatus(friend)) onComplete {
      case Success(response) =>
        $scope.$apply { () =>
          friend.status = response.status
          friend.gamesCompleted = response.gamesCompleted
          friend.gamesCreated = response.gamesCreated
          friend.gamesDeleted = response.gamesDeleted
          friend.lastLoginTime = response.lastLoginTime
        }
      case Failure(e) =>
        console.error(s"Error loading profile for ${friend.name}: ${e.getMessage}")
    }
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
trait HomeControllerScope extends Scope with GlobalNavigation {
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