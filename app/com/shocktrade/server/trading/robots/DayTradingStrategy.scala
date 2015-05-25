package com.shocktrade.server.trading.robots

import com.ldaniels528.commons.helpers.OptionHelper.Risky._
import com.shocktrade.models.quote.QuoteFilter

/**
 * Day-Trading Strategy
 * @author lawrence.daniels@gmail.com
 */
object DayTradingStrategy extends TradingStrategy {

  override def getFilter = QuoteFilter(
    priceMax = 1.0d,
    spreadMin = 25d,
    changeMax = -25d,
    volumeMin = 500000L
  )

}
