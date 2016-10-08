package com.shocktrade.client.profile

import com.shocktrade.client.MySessionService
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Timeout, _}
import org.scalajs.dom.console
import org.scalajs.nodejs.social.facebook.TaggableFriend
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr

/**
  * Home Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class HomeController($scope: HomeControllerScope, $timeout: Timeout, toaster: Toaster,
                     @injected("MySessionService") mySession: MySessionService,
                     @injected("ProfileService") profileService: ProfileService)
  extends Controller {

  $scope.selectedFriend = null

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.initHome = () => {
    $timeout(() => if ($scope.selectedFriend == null) $scope.selectedFriend = mySession.fbFriends_?.headOption.orNull, 5.seconds)
    ()
  }

  $scope.getAwards = () => mySession.userProfile.awards getOrElse emptyArray

  $scope.getFriends = () => mySession.fbFriends_?

  $scope.getNextLevelXP = () => mySession.userProfile.nextLevelXP

  $scope.getStars = () => (1 to mySession.userProfile.rep.getOrElse(3)).toJSArray

  $scope.getTotalXP = () => mySession.userProfile.totalXP.getOrElse(0)

  $scope.selectFriend = (friendOpt: js.UndefOr[TaggableFriend]) => friendOpt foreach { friend =>
    console.log(s"selecting friend ${angular.toJson(friend)}")
    $scope.selectedFriend = friend

    if (!isDefined(friend.dynamic.profile)) {
      $timeout(() => {
        friend.dynamic.profile = JS()
        friend.dynamic.error = "Failure to load status information"
      }, 3.seconds)
      /*
      profileService.getProfileByFacebookID(friend.userID.as[String]) onComplete {
        case Success(profile) =>
          friend.profile = profile
        case Failure(e) =>
          friend.profile = JS()
          friend.error = e.getMessage
          console.error(s"Error loading profile for ${friend.userID}: ${e.getMessage}")
  }*/
    }
  }

}

/**
  * Home Controller Scope
  */
@js.native
trait HomeControllerScope extends Scope {
  // variables
  var selectedFriend: TaggableFriend = js.native

  // functions
  var initHome: js.Function0[Unit] = js.native
  var getAwards: js.Function0[js.Array[String]] = js.native
  var getFriends: js.Function0[js.Array[TaggableFriend]] = js.native
  var getNextLevelXP: js.Function0[js.UndefOr[Int]] = js.native
  var getStars: js.Function0[js.Array[Int]] = js.native
  var getTotalXP: js.Function0[Int] = js.native
  var selectFriend: js.Function1[UndefOr[TaggableFriend], Unit] = js.native
}