package com.shocktrade.client

import scala.scalajs.js

/**
  * Represents an Award
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Award(val name: String,
            val code: String,
            val icon: String,
            val description: String) extends js.Object

/**
  * Award Singleton
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Award {
  val FACEBOOK = "FACEBOOK"
  val FBLIKEUS = "FBLIKEUS"
  val TWITTER = "TWITTER"
  val LINKEDIN = "LINKEDIN"
  val GOOGPLUS = "GOOGPLUS"
  val INSTGRAM = "INSTGRAM"
  val MEPROMO = "MEPROMO"
  val SOCLITE = "SOCLITE"
  val PERKS = "PERKS"
  val PERKSET = "PERKSET"
  val EUROTACT = "EUROTACT"
  val INTNSHPR = "INTNSHPR"
  val PAYDIRT = "PAYDIRT"
  val MADMONEY = "MADMONEY"
  val CRYSTBAL = "CRYSTBAL"
  val CHKDFLAG = "CHKDFLAG"
  val GLDTRPHY = "GLDTRPHY"

  /**
    * Represents the collection of contest awards
    */
  val AvailableAwards = js.Array(
    new Award(
      name = "Told your friends!",
      code = FACEBOOK,
      icon = "/images/awards/facebook.jpg",
      description = "Posted to FaceBook from ShockTrade"),
    new Award(
      name = "Right back at cha!",
      code = FBLIKEUS,
      icon = "/images/awards/facebook.jpg",
      description = "Told your friends and \"Liked\" ShockTrade on FaceBook (Pays 1 Perk)"),
    new Award(
      name = "A Little bird told me...",
      code = TWITTER,
      icon = "/images/awards/twitter.png",
      description = "Posted a Tweet from ShockTrade"),
    new Award(
      name = "Your colleagues had to know!",
      code = LINKEDIN,
      icon = "/images/awards/linkedin.png",
      description = "Posted to LinkedIn from ShockTrade"),
    new Award(
      name = "Told your followers!",
      code = GOOGPLUS,
      icon = "/images/awards/google_plus.jpg",
      description = "Posted to Google+ from ShockTrade"),
    new Award(
      name = "A Picture is worth a thousand words!",
      code = INSTGRAM,
      icon = "/images/awards/instagram.png",
      description = "Posted to Instagram from ShockTrade"),
    new Award(
      name = "Self-promotion pays!",
      code = MEPROMO,
      icon = "/images/awards/instagram.png",
      description = "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (Pays 1 Perk)"),
    new Award(
      name = "The Ultimate Socialite!",
      code = SOCLITE,
      icon = "/images/awards/instagram.png",
      description = "Earned all social awards"),
    new Award(
      name = "Perks of the Job!",
      code = PERKS,
      icon = "/images/awards/perk.gif",
      description = "Purchased (or earned) a Perk"),
    new Award(
      name = "Perk Master!",
      code = PERKSET,
      icon = "/images/awards/perk.gif",
      description = "Used every available Perk in a single game"),
    new Award(
      name = "Euro-Tactular!",
      code = EUROTACT,
      icon = "/images/awards/euro-tactular.png",
      description = "Traded the Euro"
    ),
    new Award(
      name = "International Shopper",
      code = INTNSHPR,
      icon = "/images/awards/international_shopper.gif",
      description = "Traded three or more currencies"),
    new Award(
      name = "Pay Dirt!",
      code = PAYDIRT,
      icon = "/images/awards/pay_dirt.png",
      description = "Your portfolio gained 100% or more"),
    new Award(
      name = "Mad Money!",
      code = MADMONEY,
      icon = "/images/awards/made_money.png",
      description = "Your portfolio gained 250% or more"),
    new Award(
      name = "Crystal Ball",
      code = CRYSTBAL,
      icon = "/images/awards/crystal_ball.png",
      description = "Your portfolio gained 500% or more"),
    new Award(
      name = "Checkered Flag",
      code = CHKDFLAG,
      icon = "/images/awards/checkered_flag.png",
      description = "Finished a Game!"),
    new Award(
      name = "Gold Trophy",
      code = GLDTRPHY,
      icon = "/images/awards/gold_trophy.png",
      description = "Came in first place! (out of 14 players)"
    ))

}
