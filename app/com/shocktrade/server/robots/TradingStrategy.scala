package com.shocktrade.server.robots

import com.shocktrade.models.profile.Filter

/**
 * Represents a generic trading strategy
 * @author lawrence.daniels@gmail.com
 */
trait TradingStrategy {

  /**
   * Returns a filter which provides rules for identifying appropriate investment opportunities
   * @return the [[Filter filter]]
   */
  def getFilter: Filter

}
