package com.shocktrade.javascript

import com.shocktrade.core.AwardCodes._
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Represents an Award
 * @author lawrence.daniels@gmail.com
 */
trait Award extends js.Object {
  var name: String = js.native
  var code: String = js.native
  var icon: String = js.native
  var description: String = js.native
}

/**
 * Award Singleton
 * @author lawrence.daniels@gmail.com
 */
object Award {

  def apply(name: String,
            code: String,
            icon: String,
            description: String) = {
    val award = makeNew[Award]
    award.name = name
    award.code = code
    award.icon = icon
    award.description = description
    award
  }

  /**
   * Represents the collection of contest awards
   */
  val AvailableAwards = js.Array(
    Award(
      name = "Told your friends!",
      code = FACEBOOK.toString,
      icon = "/assets/images/awards/facebook.jpg",
      description = "Posted to FaceBook from ShockTrade"),
    Award(
      name = "Right back at cha!",
      code = FBLIKEUS.toString,
      icon = "/assets/images/awards/facebook.jpg",
      description = "Told your friends and \"Liked\" ShockTrade on FaceBook (Pays 1 Perk)"),
    Award(
      name = "A Little bird told me...",
      code = TWITTER.toString,
      icon = "/assets/images/awards/twitter.png",
      description = "Posted a Tweet from ShockTrade"),
    Award(
      name = "Your colleagues had to know!",
      code = LINKEDIN.toString,
      icon = "/assets/images/awards/linkedin.png",
      description = "Posted to LinkedIn from ShockTrade"),
    Award(
      name = "Told your followers!",
      code = GOOGPLUS.toString,
      icon = "/assets/images/awards/google_plus.jpg",
      description = "Posted to Google+ from ShockTrade"),
    Award(
      name = "A Picture is worth a thousand words!",
      code = INSTGRAM.toString,
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to Instagram from ShockTrade"),
    Award(
      name = "Self-promotion pays!",
      code = MEPROMO.toString,
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (Pays 1 Perk)"),
    Award(
      name = "The Ultimate Socialite!",
      code = SOCLITE.toString,
      icon = "/assets/images/awards/instagram.png",
      description = "Earned all social awards"),
    Award(
      name = "Perks of the Job!",
      code = PERKS.toString,
      icon = "/assets/images/awards/perk.gif",
      description = "Purchased (or earned) a Perk"),
    Award(
      name = "Perk Master!",
      code = PERKSET.toString,
      icon = "/assets/images/awards/perk.gif",
      description = "Used every available Perk in a single game"),
    Award(
      name = "Euro-Tactular!",
      code = EUROTACT.toString,
      icon = "/assets/images/awards/euro-tactular.png",
      description = "Traded the Euro"
    ),
    Award(
      name = "International Shopper",
      code = INTNSHPR.toString,
      icon = "/assets/images/awards/international_shopper.gif",
      description = "Traded three or more currencies"),
    Award(
      name = "Pay Dirt!",
      code = PAYDIRT.toString,
      icon = "/assets/images/awards/pay_dirt.png",
      description = "Your portfolio gained 100% or more"),
    Award(
      name = "Mad Money!",
      code = MADMONEY.toString,
      icon = "/assets/images/awards/made_money.png",
      description = "Your portfolio gained 250% or more"),
    Award(
      name = "Crystal Ball",
      code = CRYSTBAL.toString,
      icon = "/assets/images/awards/crystal_ball.png",
      description = "Your portfolio gained 500% or more"),
    Award(
      name = "Checkered Flag",
      code = CHKDFLAG.toString,
      icon = "/assets/images/awards/checkered_flag.png",
      description = "Finished a Game!"),
    Award(
      name = "Gold Trophy",
      code = GLDTRPHY.toString,
      icon = "/assets/images/awards/gold_trophy.png",
      description = "Came in first place! (out of 14 players)"
    ))

}
