package com.shocktrade.javascript.profile

import com.ldaniels528.scalascript.core.TimerConversions._
import com.shocktrade.javascript.{ScalaJsHelper, MySession}
import ScalaJsHelper._
import com.ldaniels528.scalascript._
import com.ldaniels528.scalascript.core.Timeout
import com.ldaniels528.scalascript.extensions.Toaster
import com.shocktrade.javascript.MySession
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
      console.log(s"selecting friend ${angular.toJson(friend)}")
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
