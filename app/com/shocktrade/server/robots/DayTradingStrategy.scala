package com.shocktrade.server.robots

import com.shocktrade.models.profile.{Condition, Filter, WrappedValue}

/**
 * Day-Trading Strategy
 * @author lawrence.daniels@gmail.com
 */
object DayTradingStrategy extends TradingStrategy {

  override def getFilter: Filter = {
    Filter(
      name = "Day-Trading Filter",
      dataSource = "Robot",
      conditions = List(
        Condition(field = "lastTrade", operator = "<=", value = WrappedValue(1.0d)),
        Condition(field = "volume", operator = ">=", value = WrappedValue(500000))
      ))
  }
}
