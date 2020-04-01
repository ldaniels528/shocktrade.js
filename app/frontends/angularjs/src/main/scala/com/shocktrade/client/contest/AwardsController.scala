package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.AwardsController._
import com.shocktrade.client.users.UserService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Awards Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsController($scope: AwardsControllerScope, toaster: Toaster,
                       @injected("UserService") userService: UserService) extends Controller {

  private val availableAwards: js.Array[Award] = js.Array(Award.availableAwards: _*)

  $scope.myAwards = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initAwards = (aUserID: js.UndefOr[String]) => aUserID foreach initAwards

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
    if ($scope.myAwardCodes.isEmpty) js.Array(availableAwards: _*) else {
      val awards = availableAwards.toList
      (awards.filter(isEarned) ::: awards.filterNot(isEarned)).toJSArray
    }
  }

  $scope.findAwardImage = (aCode: js.UndefOr[String]) => aCode flatMap findAwardImage

  $scope.isEarned = (anAward: js.UndefOr[Award]) => anAward.exists(isEarned)

  private def findAwardImage(code: String): js.UndefOr[String] = awardIconsByCode.get(code).orUndefined

  private def isEarned(award: Award): Boolean = $scope.myAwardCodes.contains(award.code)

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
