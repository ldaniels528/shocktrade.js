package com.shocktrade.client.profile

import org.scalajs.angularjs._
import org.scalajs.angularjs.http.Http
import com.shocktrade.client.profile.AwardsController._
import com.shocktrade.client.{Award, MySessionService}

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Awards Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class AwardsController($scope: AwardsControllerScope, $http: Http,
                       @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAwards = () => {
    Award.AvailableAwards map { award =>
      val myAward = award.asInstanceOf[MyAward]
      myAward.owned = mySession.getMyAwards.contains(award.code)
      myAward
    } sortBy (_.owned) reverse
  }

  $scope.getAwardImage = (aCode: js.UndefOr[String]) => {
    aCode.toOption.flatMap(code => AwardIconsByCode.get(code)).orUndefined
  }

  $scope.getMyAwards = () => {
    mySession.getMyAwards flatMap (code => AwardsByCode.get(code))
  }

}

/**
  * Awards Controller Singleton
  */
object AwardsController {

  private val AwardsByCode = js.Dictionary[Award](Award.AvailableAwards map { award => (award.code, award) }: _*)

  private val AwardIconsByCode = js.Dictionary[String](Award.AvailableAwards map { award => (award.code, award.icon) }: _*)

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