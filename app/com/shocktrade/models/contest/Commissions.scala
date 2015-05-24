package com.shocktrade.models.contest

import com.shocktrade.models.contest.PerkTypes.PerkType
import com.shocktrade.models.contest.PriceTypes._

/**
 * Commissions
 * @author lawrence.daniels@gmail.com
 */
object Commissions {
  val NO_COMMISSION: Double = 0.00
  val LIMIT_COMMISSION: Double = 14.99
  val MARKET_COMMISSION: Double = 9.99
  val MARKET_AT_CLOSE_COMMISSION: Double = 7.99

  /**
   * Returns the commission cost for the given price type
   * @param priceType the given [[PriceTypes price type]]
   * @param perks the given collection of perks owned by the player
   * @return the commission cost for the given price type
   */
  def getCommission(priceType: PriceType, perks: Seq[PerkType] = Nil): Double = {
    if (perks.contains(PerkTypes.FEEWAIVR)) NO_COMMISSION
    else priceType match {
      case MARKET => MARKET_COMMISSION
      case MARKET_ON_CLOSE => MARKET_AT_CLOSE_COMMISSION
      case _ => LIMIT_COMMISSION
    }
  }


}
