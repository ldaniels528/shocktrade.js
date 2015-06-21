package com.shocktrade.javascript.profile

import biz.enef.angulate.core.HttpService
import biz.enef.angulate.{ScopeController, named}
import com.shocktrade.core.Award
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.profile.AwardsController._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Awards Controller
 * @author lawrence.daniels@gmail.com
 */
class AwardsController($scope: js.Dynamic, $http: HttpService, @named("MySession") mySession: MySession) extends ScopeController {

  $scope.getAwards = () => availableAwards

  $scope.getMyAwards = () => getMyAwards

  $scope.getAwardImage = (code: String) => awardIconsByCode.get(code).orNull

  $scope.setupAwards = () => setupAwards()

  // watch for changes to the player's profile
  $scope.$watch(mySession.userProfile, (newProfile: js.Dynamic, oldProfile: js.Dynamic) => setupAwards())

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def getMyAwards: js.Array[js.Dynamic] = {
    mySession.getMyAwards() map (code => awardsByCode.get(code).orNull)
  }

  private def setupAwards() {
    g.console.log("Setting up awards....")
    availableAwards foreach { award =>
      award.owned = mySession.getMyAwards().contains(award.code.as[String])
    }
  }

}

/**
 * Awards Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object AwardsController {

  // define all available awards
  private val availableAwards = js.Array[js.Dynamic](
    Award.availableAwards
      .map (a => JS(name = a.name, code = a.code.toString, icon = a.icon, description = a.description)): _*)
      .sortBy(_.owned.as[Boolean])
      .reverse

  private val awardsByCode = js.Dictionary[js.Dynamic](
    availableAwards map { award => (award.code.as[String], award) }: _*
  )

  private val awardIconsByCode = js.Dictionary[String](
    availableAwards map { award => (award.code.as[String], award.icon.as[String]) }: _*
  )

}