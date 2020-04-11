package com.shocktrade.client.contest

import com.shocktrade.client.users.UserService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Awards Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait AwardsSupport {
  ref: Controller =>

  private val availableAwards: js.Array[Award] = Award.availableAwards
  private val awardIconsByCode: js.Dictionary[String] = js.Dictionary(Award.availableAwards.map(award => award.code -> award.icon): _*)

  $scope.myAwards = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Injected Functions
  ///////////////////////////////////////////////////////////////////////////

  def $scope: AwardsSupportScope

  def toaster: Toaster

  def userService: UserService

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initAwards = (aUserID: js.UndefOr[String]) => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    aUserID map initAwards
  }

  protected def initAwards(userID: String): js.Promise[HttpResponse[js.Array[String]]] = {
    val outcome = userService.findMyAwards(userID)
    outcome onComplete {
      case Success(myAwardCodes) =>
        $scope.$apply { () =>
          $scope.myAwards = availableAwards.filter(a => myAwardCodes.data.contains(a.code))
          $scope.myAwardCodes = myAwardCodes.data
        }
      case Failure(e) =>
        toaster.error("Failed to retrieve awards")
        console.error(s"Failed to retrieve awards: ${e.displayMessage}")
    }
    outcome
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

  $scope.getMyAwards = () => $scope.myAwards

  $scope.findAwardImage = (aCode: js.UndefOr[String]) => aCode flatMap findAwardImage

  $scope.isEarned = (anAward: js.UndefOr[Award]) => anAward.exists(isEarned)

  protected def findAwardImage(code: String): js.UndefOr[String] = awardIconsByCode.get(code).orUndefined

  protected def isEarned(award: Award): Boolean = $scope.myAwardCodes.exists(_.contains(award.code))

}

/**
 * Awards Support Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait AwardsSupportScope extends Scope {
  // functions
  var initAwards: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[js.Array[String]]]]] = js.native
  var getAwards: js.Function0[js.UndefOr[js.Array[Award]]] = js.native
  var getMyAwards: js.Function0[js.UndefOr[js.Array[Award]]] = js.native
  var findAwardImage: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var isEarned: js.Function1[js.UndefOr[Award], Boolean] = js.native

  // variables
  var myAwards: js.UndefOr[js.Array[Award]] = js.native
  var myAwardCodes: js.UndefOr[js.Array[String]] = js.native

}
