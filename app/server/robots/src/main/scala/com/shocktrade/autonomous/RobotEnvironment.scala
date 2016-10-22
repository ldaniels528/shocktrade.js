package com.shocktrade.autonomous

import com.shocktrade.server.dao.contest.{OrderData, PortfolioData, PositionData}

/**
  * Represents a robot's operating environment
  * @param portfolio the given [[PortfolioData portfolio]]
  * @param positions the given array of [[PositionData positions]]
  * @param orders    the given array of [[OrderData orders]]
  */
class RobotEnvironment(val name: String,
                       val portfolio: PortfolioData,
                       val positions: Seq[PositionData],
                       val orders: Seq[OrderData]) {

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

  def apply(name: String, portfolio: PortfolioData) = {
    new RobotEnvironment(
      name = name,
      portfolio = portfolio,
      positions = portfolio.positions.toList.flatMap(_.toList),
      orders = portfolio.orders.toList.flatMap(_.toList)
    )
  }

}