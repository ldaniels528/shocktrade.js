package com.shocktrade.webapp.routes.autotrading

import com.shocktrade.webapp.routes.account.dao.UserProfileData
import com.shocktrade.webapp.routes.autotrading.dao.RobotData

import scala.concurrent.ExecutionContext

/**
 * Represents a Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait TradingStrategy {

  def operate(robot: RobotData): Unit

}

/**
 * Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TradingStrategy {
  val PENNY_STOCK = "penny-stock"

  def withName(strategyName: String)(implicit ec: ExecutionContext): Option[TradingStrategy] = strategyName match {
    case "penny-stock" => Some(new PennyStockTradingStrategy())
    case _ => None
  }

}