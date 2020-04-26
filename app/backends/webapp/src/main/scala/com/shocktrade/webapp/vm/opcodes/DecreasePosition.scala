package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.routes.contest.dao.PositionData

import scala.concurrent.{ExecutionContext, Future}

/**
 * Decrease Position OpCode
 * @param portfolioID the given portfolio ID
 * @param orderID     the given order ID
 * @param symbol      the given symbol
 * @param exchange    the given exchange
 * @param quantity    the given quantity
 * @param proceeds    the given proceeds
 */
case class DecreasePosition(portfolioID: String, orderID: String, symbol: String, exchange: String, quantity: Double, proceeds: Double)
  extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.decreasePosition(orderID = orderID, proceeds = proceeds, position = new PositionData(
      portfolioID = portfolioID,
      symbol = symbol,
      exchange = exchange,
      quantity = quantity
    ))
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, orderID: $orderID, symbol: $symbol, quantity: $quantity, proceeds: $proceeds)"

}