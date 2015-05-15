package com.shocktrade.models.profile

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Represents a Perk
 * @author lawrence.daniels@gmail.com
 */
case class Perk(name: String,
                code: String,
                description: String,
                cost: Double)

/**
 * Perk Singleton
 * @author lawrence.daniels@gmail.com
 */
object Perk {

  implicit val perkReads: Reads[Perk] = (
    (__ \ "name").read[String] and
      (__ \ "code").read[String] and
      (__ \ "description").read[String] and
      (__ \ "cost").read[Double])(Perk.apply _)

  implicit val perkWrites: Writes[Perk] = (
    (__ \ "name").write[String] and
      (__ \ "code").write[String] and
      (__ \ "description").write[String] and
      (__ \ "cost").write[Double])(unlift(Perk.unapply))

  val allPerks = Seq(
    Perk(code = "CREATOR",
      name = "Game Creator",
      cost = 10000,
      description = "Gives the player the ability to  create new custom games"
    ),
    Perk(
      code = "PRCHEMNT",
      name = "Purchase Eminent",
      cost = 10000,
      description = "Gives the player the ability to create SELL orders for securities not yet owned"
    ),
    Perk(
      code = "PRFCTIMG",
      name = "Perfect Timing",
      cost = 25000,
      description = "Gives the player the ability to create BUY orders for more than cash currently available"
    ),
    Perk(
      code = "CMPDDALY",
      name = "Compounded Daily",
      cost = 50000,
      description = "Gives the player the ability to earn interest on cash not currently invested"
    ),
    Perk(
      code = "FEEWAIVR",
      name = "Fee Waiver",
      cost = 50000,
      description = "Reduces the commissions the player pays for buying or selling securities"
    ),
    Perk(
      code = "MARGIN",
      name = "Rational People think at the Margin",
      cost = 100000,
      description = "Gives the player the ability to use margin accounts"
    ),
    Perk(
      code = "SAVGLOAN",
      name = "Savings and Loans",
      cost = 100000,
      description = "Gives the player the ability to borrow money"
    ),
    Perk(
      code = "LOANSHRK",
      name = "Loan Shark",
      cost = 100000,
      description = "Gives the player the ability to loan other players money at any interest rate"
    ),
    Perk(
      code = "MUTFUNDS",
      name = "The Feeling's Mutual",
      cost = 100000,
      description = "Gives the player the ability to create and use mutual funds"
    ),
    Perk(
      code = "RISKMGMT",
      name = "Risk Management",
      cost = 100000,
      description = "Gives the player the ability to trade options"
    ))

}