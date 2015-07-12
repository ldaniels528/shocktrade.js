package com.shocktrade.javascript.profile

import com.github.ldaniels528.scalascript._
import com.github.ldaniels528.scalascript.core.Timeout
import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.social.TaggableFriend
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * Home Controller
 * @author lawrence.daniels@gmail.com
 */
class HomeController($scope: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                     @injected("MySession") mySession: MySession,
                     @injected("ProfileService") profileService: ProfileService)
  extends Controller {

  private var selectedFriend: TaggableFriend = null

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

  $scope.getNextLevelXP = () => mySession.userProfile.nextLevelXP.getOrElse(0)

  $scope.getSelectedFriend = () => selectedFriend

  $scope.selectFriend = (friend: TaggableFriend) => selectFriend(friend)

  $scope.getStars = () => js.Array(1 to mySession.userProfile.rep.getOrElse(3): _*)

  $scope.getTotalXP = () => mySession.userProfile.totalXP.getOrElse(0)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def selectFriend = (friend: TaggableFriend) => {
    if (isDefined(friend)) {
      console.log(s"selecting friend ${angular.toJson(friend)}")
      selectedFriend = friend

      if (!isDefined(friend.dynamic.profile)) {
        $timeout({ () =>
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
            g.console.error(s"Error loading profile for ${friend.userID}: ${e.getMessage}")
    }*/
      }
    }
  }

}
