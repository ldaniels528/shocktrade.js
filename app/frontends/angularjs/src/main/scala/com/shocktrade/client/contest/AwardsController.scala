package com.shocktrade.client.contest

import com.shocktrade.client.contest.AwardsController._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.{Scope, _}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Awards Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsController($scope: AwardsControllerScope,
                       @injected("AwardService") awardService: AwardService) extends Controller {

  $scope.awards = js.Array()

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAwards = () => $scope.awards

  $scope.getAwardImage = (aCode: js.UndefOr[String]) => getAwardImage(aCode)

  $scope.getMyAwards = () => getMyAwards

  ///////////////////////////////////////////////////////////////////////////
  //          Private Methods
  ///////////////////////////////////////////////////////////////////////////

  private def loadAwards(userID: String): Unit = {
    awardService.findByUser(userID).toFuture onComplete {
      case Success(awards) => $scope.awards = awards.data
      case Failure(e) => console.error(e.displayMessage)
    }
  }

  private def getMyAwards: js.Array[MyAward] = {
    Award.availableAwards map { award =>
      val myAward = award.asInstanceOf[MyAward]
      myAward.owned = false //mySession.getMyAwards.contains(award.code)
      myAward
    } sortBy (_.owned) reverse
  }

  private def getAwardImage(aCode: js.UndefOr[String]): js.UndefOr[String] = {
    aCode.toOption.flatMap(AwardIconsByCode.get).orUndefined
  }

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
  // variables
  var awards: js.Array[Award] = js.native

  // functions
  var getAwards: js.Function0[js.Array[Award]] = js.native
  var getAwardImage: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var getMyAwards: js.Function0[js.Array[MyAward]] = js.native
}

/**
 * Award with owned information
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait MyAward extends Award {
  var owned: Boolean = js.native
}