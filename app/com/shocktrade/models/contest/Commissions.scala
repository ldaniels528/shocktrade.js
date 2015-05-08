package com.shocktrade.models.contest

import com.shocktrade.models.contest.PriceType._

/**
 * Commissions
 * @author lawrence.daniels@gmail.com
 */
object Commissions {

  def getCommission(priceType: PriceType) = {
    priceType match {
      case LIMIT => forLimit
      case MARKET => forMarket
      case MARKET_ON_CLOSE => forMarketAtClose
    }
  }

  def forLimit: BigDecimal = 14.99

  def forMarket: BigDecimal = 9.99

  def forMarketAtClose: BigDecimal = 7.99

}
