package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.OrderConstants
import com.shocktrade.common.models.contest.OrderOutcome

import scala.concurrent.{ExecutionContext, Future}

/**
 * Decrease Position OpCode
 * @param portfolioID the given portfolio ID
 * @param orderID     the given order ID
 * @param priceType    the given price type
 * @param symbol      the given symbol
 * @param exchange    the given exchange
 * @param quantity    the given quantity
 */
case class DecreasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[OrderOutcome] = {
    try ctx.decreasePosition(portfolioID, orderID, priceType, symbol, exchange, quantity) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "portfolioID" -> portfolioID,
    "orderID" -> orderID,
    "orderType" -> OrderConstants.SELL,
    "priceType" -> priceType,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "quantity" -> quantity
  )

}