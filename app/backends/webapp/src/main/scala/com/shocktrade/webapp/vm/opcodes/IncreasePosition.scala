package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.dao.PositionData

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

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
    try ctx.increasePosition(orderID = orderID, cost = cost, position = new PositionData(
      positionID = js.undefined,
      portfolioID = portfolioID,
      symbol = symbol,
      exchange = exchange,
      quantity = quantity
    )) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "portfolioID" -> portfolioID,
    "orderID" -> orderID,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "quantity" -> quantity,
    "cost" -> cost
  )

}