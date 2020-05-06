package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.OrderConstants
import com.shocktrade.webapp.vm.dao.PositionData

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

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
    try ctx.decreasePosition(orderID = orderID, proceeds = proceeds, position = new PositionData(
      positionID = js.undefined,
      portfolioID = portfolioID,
      symbol = symbol,
      exchange = exchange,
      quantity = quantity,
      marketValue = js.undefined,
      processedTime = js.undefined
    )) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "portfolioID" -> portfolioID,
    "orderID" -> orderID,
    "orderType" -> OrderConstants.SELL,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "quantity" -> quantity,
    "proceeds" -> proceeds
  )

}