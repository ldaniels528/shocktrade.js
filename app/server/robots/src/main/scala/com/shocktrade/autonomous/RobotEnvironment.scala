package com.shocktrade.autonomous

import com.shocktrade.common.models.contest.{OrderLike, PortfolioLike, PositionLike}

/**
  * Represents a robot's operating environment
  * @param portfolio the given [[PortfolioLike portfolio]]
  * @param positions the given array of [[PositionLike positions]]
  * @param orders    the given array of [[OrderLike orders]]
  */
class RobotEnvironment(val portfolio: PortfolioLike,
                       val positions: Seq[_ <: PositionLike],
                       val orders: Seq[_ <: OrderLike]) {

  /**
    * Computes the outstanding orders cost
    * @return the outstanding orders cost
    */
  def outstandingOrdersCost = orders.flatMap(_.totalCost.toOption).sum

}

/**
  * Robot Environment Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RobotEnvironment {

  def apply(portfolio: PortfolioLike) = new RobotEnvironment(
    portfolio = portfolio,
    positions = portfolio.positions.toList.flatMap(_.toList),
    orders = portfolio.orders.toList.flatMap(_.toList)
  )

}