package com.shocktrade.client.contest

import com.shocktrade.client.MySessionService
import com.shocktrade.client.contest.AwardsController._
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.{Scope, _}

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Awards Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsController($scope: AwardsControllerScope, $http: Http,
                       @injected("MySessionService") mySession: MySessionService) extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAwards = () => getAwards

  $scope.getAwardImage = (aCode: js.UndefOr[String]) => getAwardImage(aCode)

  $scope.getMyAwards = () => getMyAwards

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def getAwards: js.Array[MyAward] = {
    Award.availableAwards map { award =>
      val myAward = award.asInstanceOf[MyAward]
      myAward.owned = mySession.getMyAwards.contains(award.code)
      myAward
    } sortBy (_.owned) reverse
  }

  private def getAwardImage(aCode: js.UndefOr[String]): js.UndefOr[String] = {
    aCode.toOption.flatMap(AwardIconsByCode.get).orUndefined
  }

  private def getMyAwards: js.Array[Award] = mySession.getMyAwards flatMap AwardsByCode.get

}

/**
 * Awards Controller Singleton
 */
object AwardsController {
  private val AwardsByCode: js.Dictionary[Award] = js.Dictionary(Award.availableAwards.map(award => award.code -> award): _*)
  private val AwardIconsByCode: js.Dictionary[String] = js.Dictionary(Award.availableAwards.map(award => award.code -> award.icon): _*)

}

/**
 * Awards Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait AwardsControllerScope extends Scope {
  var getAwards: js.Function0[js.Array[MyAward]] = js.native
  var getAwardImage: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var getMyAwards: js.Function0[js.Array[Award]] = js.native
}

/**
 * Award with owned information
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait MyAward extends Award {
  var owned: Boolean = js.native
}