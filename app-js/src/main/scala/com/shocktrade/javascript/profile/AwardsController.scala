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

  $scope.getMyAwards = () => getMyAwards(mySession.userProfile)

  $scope.getAwardImage = (code: String) => awardIconsByCode.get(code).orNull

  $scope.setupAwards = () => setupAwards(mySession.userProfile)

  // watch for changes to the player's profile
  $scope.$watch(mySession.userProfile, (newProfile: js.Dynamic, oldProfile: js.Dynamic) => {
    if (newProfile != oldProfile) {
      g.console.log(s"User ID changed from ${oldProfile.OID} to ${newProfile.OID}")
      setupAwards(newProfile)
    }
  })

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def getMyAwards(userProfile: js.Dynamic): js.Array[js.Dynamic] = {
    userProfile.awards.asArray[String] map (code => awardsByCode.get(code).orNull)
  }

  private def setupAwards(userProfile: js.Dynamic) {
    if (isDefined(userProfile)) {
      g.console.log("Setting up awards....")

      // get the award codes for the user
      val myAwardCodes = if (isDefined(userProfile.awards)) userProfile.awards.asArray[String] else emptyArray[String]

      // setup the ownership for the awards
      availableAwards foreach { award =>
        // set the ownership indicator
        award.owned = myAwardCodes.contains(award.code.as[String])
      }
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
    Award.availableAwards map (a => JS(name = a.name, code = a.code.toString, icon = a.icon, description = a.description)): _*)

  private val awardsByCode = js.Dictionary[js.Dynamic](
    availableAwards map { award => (award.code.as[String], award) }: _*
  )

  private val awardIconsByCode = js.Dictionary[String](
    availableAwards map { award => (award.code.as[String], award.icon.as[String]) }: _*
  )

}