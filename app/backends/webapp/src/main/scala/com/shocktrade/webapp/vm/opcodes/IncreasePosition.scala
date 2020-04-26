package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.routes.contest.dao.PositionData

import scala.concurrent.{ExecutionContext, Future}

/**
 * Increase Position OpCode
 * @param portfolioID the given portfolio ID
 * @param orderID     the given order ID
 * @param symbol      the given symbol
 * @param exchange    the given exchange
 * @param quantity    the given quantity
 * @param cost        the given cost
 */
case class IncreasePosition(portfolioID: String, orderID: String, symbol: String, exchange: String, quantity: Double, cost: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.increasePosition(orderID = orderID, cost = cost, position = new PositionData(
      portfolioID = portfolioID,
      symbol = symbol,
      exchange = exchange,
      quantity = quantity
    ))
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, symbol: $symbol, exchange: $exchange, quantity: $quantity, cost: $cost)"

}