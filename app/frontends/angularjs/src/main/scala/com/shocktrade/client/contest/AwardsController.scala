package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.AwardsController._
import com.shocktrade.client.users.UserService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster

import scala.language.postfixOps
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

  $scope.myAwards = js.Array()

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initAwards = (aUserID: js.UndefOr[String]) => aUserID foreach initAwards

  $scope.onUserProfileUpdated { (_, userProfile) => $scope.initAwards(userProfile.userID) }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.findAwards = () => Award.availableAwards

  $scope.findAwardImage = (aCode: js.UndefOr[String]) => aCode flatMap findAwardImage

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def initAwards(userID: String): Unit = {
    userService.findMyAwards(userID).toFuture onComplete {
      case Success(myAwardCodes) =>
        $scope.$apply(() => $scope.myAwards = Award.availableAwards.filter(a => myAwardCodes.data.contains(a.code)))
      case Failure(e) =>
        toaster.error("Failed to retrieve orders")
        console.error(s"Failed to retrieve orders: ${e.displayMessage}")
    }
  }

  private def findAwardImage(code: String): js.UndefOr[String] = awardIconsByCode.get(code).orUndefined

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
    var findAwards: js.Function0[js.Array[Award]] = js.native
    var findAwardImage: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

    // variables
    var myAwards: js.Array[Award] = js.native
  }

}
