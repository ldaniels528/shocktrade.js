package com.shocktrade.models.profile

/**
 * Represents the collection award
 * @author lawrence.daniels@gmail.com
 */
case class Award(name: String, code: AwardCode, icon: String, description: String)

/**
 * Award Singleton
 * @author lawrence.daniels@gmail.com
 */
object Award {

  /**
   * Represents the collection of contest awards
   */
  val availableAwards = Seq(
    Award(
      name = "Told your friends!",
      code = FACEBOOK,
      icon = "/assets/images/awards/facebook.jpg",
      description = "Posted to FaceBook from ShockTrade"),
    Award(
      name = "Right back at cha!",
      code = FBLIKEUS,
      icon = "/assets/images/awards/facebook.jpg",
      description = "Told your friends and \"Liked\" ShockTrade on FaceBook (Pays 1 Perk)"),
    Award(
      name = "A Little bird told me...",
      code = TWITTER,
      icon = "/assets/images/awards/twitter.png",
      description = "Posted a Tweet from ShockTrade"),
    Award(
      name = "Your colleagues had to know!",
      code = LINKEDIN,
      icon = "/assets/images/awards/linkedin.png",
      description = "Posted to LinkedIn from ShockTrade"),
    Award(
      name = "Told your followers!",
      code = GOOGPLUS,
      icon = "/assets/images/awards/google_plus.jpg",
      description = "Posted to Google+ from ShockTrade"),
    Award(
      name = "A Picture is worth a thousand words!",
      code = INSTGRAM,
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to Instagram from ShockTrade"),
    Award(
      name = "Self-promotion pays!",
      code = MEPROMO,
      icon = "/assets/images/awards/instagram.png",
      description = "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (Pays 1 Perk)"),
    Award(
      name = "The Ultimate Socialite!",
      code = SOCLITE,
      icon = "/assets/images/awards/instagram.png",
      description = "Earned all social awards"),
    Award(
      name = "Perks of the Job!",
      code = PERKS,
      icon = "/assets/images/awards/perk.gif",
      description = "Purchased (or earned) a Perk"),
    Award(
      name = "Perk Master!",
      code = PERKSET,
      icon = "/assets/images/awards/perk.gif",
      description = "Used every available Perk in a single game"),
    Award(
      name = "Euro-Tactular!",
      code = EUROTACT,
      icon = "/assets/images/awards/euro-tactular.png",
      description = "Traded the Euro"
    ),
    Award(
      name = "International Shopper",
      code = INTNSHPR,
      icon = "/assets/images/awards/international_shopper.gif",
      description = "Traded three or more currencies"),
    Award(
      name = "Pay Dirt!",
      code = PAYDIRT,
      icon = "/assets/images/awards/pay_dirt.png",
      description = "Your portfolio gained 100% or more"),
    Award(
      name = "Mad Money!",
      code = MADMONEY,
      icon = "/assets/images/awards/made_money.png",
      description = "Your portfolio gained 250% or more"),
    Award(
      name = "Crystal Ball",
      code = CRYSTBAL,
      icon = "/assets/images/awards/crystal_ball.png",
      description = "Your portfolio gained 500% or more"),
    Award(
      name = "Checkered Flag",
      code = CHKDFLAG,
      icon = "/assets/images/awards/checkered_flag.png",
      description = "Finished a Game!"),
    Award(
      name = "Gold Trophy",
      code = GLDTRPHY,
      icon = "/assets/images/awards/gold_trophy.png",
      description = "Came in first place! (out of 14 players)"
    ))

}
