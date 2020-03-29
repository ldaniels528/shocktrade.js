package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.webapp.routes.contest.dao.OrderData
import io.scalajs.JSON

import scala.scalajs.js

/**
 * Represents a contest/trading activity
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
sealed trait RobotActivity {

  def counts: Int

  def robot: RobotRef

  def toMap: js.Dictionary[Any]

  override def toString: String = JSON.stringify(toMap)

}

case class JoinedContestActivity(robot: RobotRef, contestRefs: js.Array[ContestRef], counts: Int) extends RobotActivity {
  override def toMap: js.Dictionary[Any] = js.Dictionary("type" -> getClass.getSimpleName, "robot" -> robot, "contestRefs" -> contestRefs, "counts" -> counts)
}

case class OrderBuyActivity(robot: RobotRef, stocks: js.Array[ResearchQuote], orders: js.Array[OrderData], counts: Int) extends RobotActivity {
  override def toMap: js.Dictionary[Any] = js.Dictionary("type" -> getClass.getSimpleName, "robot" -> robot, "stocks" -> stocks, "orders" -> orders, "counts" -> counts)
}

case class OrderSellActivity(robot: RobotRef, stocks: js.Array[ResearchQuote], orders: js.Array[OrderData], counts: Int) extends RobotActivity {
  override def toMap: js.Dictionary[Any] = js.Dictionary("type" -> getClass.getSimpleName, "robot" -> robot, "stocks" -> stocks, "orders" -> orders, "counts" -> counts)
}

