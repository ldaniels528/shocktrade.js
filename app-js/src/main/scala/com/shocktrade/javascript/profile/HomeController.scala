package com.shocktrade.javascript.profile

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.{Controller, Timeout}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Home Controller
 * @author lawrence.daniels@gmail.com
 */
class HomeController($scope: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                     @named("MySession") mySession: MySession,
                     @named("ProfileService") profileService: ProfileService)
  extends Controller {

  private var selectedFriend: js.Dynamic = null

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.initHome = () => {
    $timeout(() =>
      if (selectedFriend == null) {
        mySession.fbFriends.headOption foreach (selectedFriend = _)
      }, 5.seconds)
  }

  $scope.getAwards = () => mySession.userProfile.awards.asArray[js.Dynamic]

  $scope.getFriends = () => mySession.fbFriends

  $scope.getNextLevelXP = () => mySession.userProfile.nextLevelXP.asOpt[Double].getOrElse(0d)

  $scope.getSelectedFriend = () => selectedFriend

  $scope.selectFriend = (friend: js.Dynamic) => selectFriend(friend)

  $scope.getStars = () => js.Array(1 to mySession.userProfile.rep.asOpt[Int].getOrElse(3): _*)

  $scope.getTotalXP = () => mySession.userProfile.totalXP.asOpt[Double].getOrElse(0d)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def selectFriend = (friend: js.Dynamic) => {
    if (isDefined(friend)) {
      g.console.log(s"selecting friend ${toJson(friend)}")
      selectedFriend = friend

      if (!isDefined(friend.profile)) {
        $timeout({ () =>
          friend.profile = JS()
          friend.error = "Failure to load status information"
        }, 3.seconds)
        /*
        profileService.getProfileByFacebookID(friend.userID.as[String]) onComplete {
          case Success(profile) =>
            friend.profile = profile
          case Failure(e) =>
            friend.profile = JS()
            friend.error = e.getMessage
            g.console.error(s"Error loading profile for ${friend.userID}: ${e.getMessage}")
    }*/
      }
    }
  }

}
