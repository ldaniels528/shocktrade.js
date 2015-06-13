package com.shocktrade.javascript.profile

import biz.enef.angulate.core.HttpService
import biz.enef.angulate.{ScopeController, named}
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

  $scope.getMyAwards = (userProfile: js.Dynamic) => getMyAwards(userProfile)

  $scope.getAwardImage = (code: String) => awardIconsByCode.get(code).orNull

  $scope.setupAwards = (userProfile: js.Dynamic) => setupAwards(userProfile)

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
  private val availableAwards = js.Array(
    JS(
      name = "Told your friends!",
      code = "FACEBOOK",
      icon = "/assets/images/awards/facebook.jpg",
      description = "Posted to FaceBook from ShockTrade"),
    JS(
      name = "Right back at cha!",
      code = "FBLIKEUS",
      icon = "/assets/images/awards/facebook.jpg",
      description = "Told your friends and \"Liked\" ShockTrade on FaceBook (Pays 1 Perk)"),
    JS(
      name = "A Little bird told me...",
      code = "TWITTER",
      icon = "/assets/images/awards/twitter.png",
      description = "Posted a Tweet from ShockTrade"),
    JS(
      name = "Your colleagues had to know!",
      code = "LINKEDIN",
      icon = "/assets/images/awards/linkedin.png",
      description = "Posted to LinkedIn from ShockTrade"),
    JS(
      name = "Told your followers!",
      code = "GOOGPLUS",
      icon = "/assets/images/awards/google_plus.jpg",
      description = "Posted to Google+ from ShockTrade"),
    JS(
      name = "A Picture is worth a thousand words!",
      code = "INSTGRAM",
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to Instagram from ShockTrade"),
    JS(
      name = "Self-promotion pays!",
      code = "MEPROMO",
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (Pays 1 Perk)"),
    JS(
      name = "The Ultimate Socialite!",
      code = "SOCLITE",
      icon = "/assets/images/awards/instagram.png",
      description = "Earned all social awards"),
    JS(
      name = "Perks of the Job!",
      code = "PERK",
      icon = "/assets/images/awards/perk.gif",
      description = "Purchased (or earned) a Perk"),
    JS(
      name = "It's time for the Perk-u-lator!",
      code = "5PERKS",
      icon = "/assets/images/awards/perk.gif",
      description = "Purchased (or earned) 5 Perks"),
    JS(
      name = "Perk Master!",
      code = "10PERKS",
      icon = "/assets/images/awards/perk.gif",
      description = "Purchased (or earned) 10 Perks"),
    JS(
      name = "Euro-Tactular!",
      code = "EUROTACT",
      icon = "/assets/images/awards/euro-tactular.png",
      description = "Traded the Euro"
    ),
    JS(
      name = "International Shopper",
      code = "INTNSHPR",
      icon = "/assets/images/awards/international_shopper.gif",
      description = "Traded three or more currencies"),
    JS(
      name = "Pay Dirt!",
      code = "PAYDIRT",
      icon = "/assets/images/awards/pay_dirt.png",
      description = "Your portfolio gained 100% or more"),
    JS(
      name = "Mad Money!",
      code = "MADMONEY",
      icon = "/assets/images/awards/made_money.png",
      description = "Your portfolio gained 250% or more"),
    JS(
      name = "Crystal Ball",
      code = "CRYSTBAL",
      icon = "/assets/images/awards/crystal_ball.png",
      description = "Your portfolio gained 500% or more"),
    JS(
      name = "Checkered Flag",
      code = "CHKDFLAG",
      icon = "/assets/images/awards/checkered_flag.png",
      description = "Finished a Game!"),
    JS(
      name = "Gold Trophy",
      code = "GLDTRPHY",
      icon = "/assets/images/awards/gold_trophy.png",
      description = "Came in first place! (out of 14 players)"
    ))

  private val awardsByCode = js.Dictionary[js.Dynamic](
    availableAwards map { award => (award.code.as[String], award) }: _*
  )

  private val awardIconsByCode = js.Dictionary[String](
    availableAwards map { award => (award.code.as[String], award.icon.as[String]) }: _*
  )

}