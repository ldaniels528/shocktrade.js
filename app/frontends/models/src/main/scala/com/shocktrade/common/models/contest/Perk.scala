package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Perk
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Perk(val name: String,
           val code: String,
           val description: String,
           val cost: Double,
           var owned: js.UndefOr[Boolean] = js.undefined,
           var selected: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
 * Perk Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Perk {
  val availablePerks: js.Array[Perk] = js.Array(
    new Perk(name = "Purchase Eminent", code = "PRCHEMNT", cost = 500, description = "Gives the player the ability to create SELL orders for securities not yet owned"),
    new Perk(name = "Perfect Timing", code = "PRFCTIMG", cost = 500, description = "Gives the player the ability to create BUY orders for more than cash currently available"),
    new Perk(name = "Compounded Daily", code = "CMPDDALY", cost = 1000, description = "Gives the player the ability to earn interest on cash not currently invested"),
    new Perk(name = "Fee Waiver", code = "FEEWAIVR", cost = 2500, description = "Reduces the commissions the player pays for buying or selling securities"),
    new Perk(name = "Rational People think at the Margin", code = "MARGIN", cost = 2500, description = "Gives the player the ability to use margin accounts"),
    new Perk(name = "Savings and Loans", code = "SAVGLOAN", cost = 5000, description = "Gives the player the ability to borrow money"),
    new Perk(name = "Loan Shark", code = "LOANSHRK", cost = 5000, description = "Gives the player the ability to loan other players money at any interest rate"),
    new Perk(name = "The Feeling's Mutual", code = "MUTFUNDS", cost = 5000, description = "Gives the player the ability to create and use mutual funds"),
    new Perk(name = "Risk Management", code = "RISKMGMT", cost = 5000, description = "Gives the player the ability to trade options")
  )

}