package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.AwardsController._
import com.shocktrade.client.users.UserService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Awards Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsController($scope: AwardsControllerScope, $cookies: Cookies, toaster: Toaster,
                       @injected("UserService") userService: UserService) extends Controller {

  implicit private val cookies: Cookies = $cookies

  private val availableAwards: js.Array[Award] = Award.availableAwards

  $scope.myAwards = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initAwards = (aUserID: js.UndefOr[String]) => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    aUserID foreach initAwards
  }

  $scope.onContestSelected { (_, _) => $scope.initAwards($cookies.getGameState.userID) }

  $scope.onUserProfileUpdated { (_, userProfile) => $scope.initAwards(userProfile.userID) }

  private def initAwards(userID: String): Unit = {
    userService.findMyAwards(userID).toFuture onComplete {
      case Success(myAwardCodes) =>
        $scope.$apply { () =>
          $scope.myAwards = availableAwards.filter(a => myAwardCodes.data.contains(a.code))
          $scope.myAwardCodes = myAwardCodes.data
        }
      case Failure(e) =>
        toaster.error("Failed to retrieve awards")
        console.error(s"Failed to retrieve awards: ${e.displayMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAwards = () => {
    if ($scope.myAwardCodes.isEmpty) availableAwards else {
      val awards = availableAwards.toList
      (awards.filter(isEarned) ::: awards.filterNot(isEarned)).toJSArray
    }
  }

  $scope.findAwardImage = (aCode: js.UndefOr[String]) => aCode flatMap findAwardImage

  $scope.isEarned = (anAward: js.UndefOr[Award]) => anAward.exists(isEarned)

  private def findAwardImage(code: String): js.UndefOr[String] = awardIconsByCode.get(code).orUndefined

  private def isEarned(award: Award): Boolean = $scope.myAwardCodes.exists(_.contains(award.code))

}

/**
 * Awards Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object AwardsController {
  private val awardIconsByCode: js.Dictionary[String] = js.Dictionary(Award.availableAwards.map(award => award.code -> award.icon): _*)

  /**
   * Awards Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait AwardsControllerScope extends Scope {
    // functions
    var initAwards: js.Function1[js.UndefOr[String], Unit] = js.native
    var getAwards: js.Function0[js.UndefOr[js.Array[Award]]] = js.native
    var findAwardImage: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
    var isEarned: js.Function1[js.UndefOr[Award], Boolean] = js.native

    // variables
    var myAwards: js.UndefOr[js.Array[Award]] = js.native
    var myAwardCodes: js.UndefOr[js.Array[String]] = js.native
  }

}
