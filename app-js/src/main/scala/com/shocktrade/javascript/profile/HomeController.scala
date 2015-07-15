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
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr

/**
 * Home Controller
 * @author lawrence.daniels@gmail.com
 */
class HomeController($scope: HomeScope, $timeout: Timeout, toaster: Toaster,
                     @injected("MySession") mySession: MySession,
                     @injected("ProfileService") profileService: ProfileService)
  extends Controller {

  $scope.selectedFriend = null

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  @scoped def initHome() {
    $timeout(() => if ($scope.selectedFriend == null) $scope.selectedFriend = mySession.fbFriends.headOption.orNull, 5.seconds)
    ()
  }

  @scoped def getAwards = mySession.userProfile.awards

  @scoped def getFriends = mySession.fbFriends

  @scoped def getNextLevelXP = mySession.userProfile.nextLevelXP

  @scoped def getStars = (1 to mySession.userProfile.rep.getOrElse(3)).toJSArray

  @scoped def getTotalXP = mySession.userProfile.totalXP.getOrElse(0)

  @scoped def selectFriend(friendMaybe: UndefOr[TaggableFriend]) = {
    friendMaybe foreach { friend =>
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
            g.console.error(s"Error loading profile for ${friend.userID}: ${e.getMessage}")
    }*/
      }
    }
  }

}

/**
 * Home Controller Scope
 */
trait HomeScope extends Scope {
  var selectedFriend: TaggableFriend = js.native
}