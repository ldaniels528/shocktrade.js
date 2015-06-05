package com.shocktrade.server.trading.robots

import com.shocktrade.models.quote.QuoteFilter

/**
 * Represents a generic trading strategy
 * @author lawrence.daniels@gmail.com
 */
trait TradingStrategy {

  /**
   * Returns a filter which provides rules for identifying appropriate investment opportunities
   * @return the [[QuoteFilter filter]]
   */
  def getFilter: QuoteFilter

}
