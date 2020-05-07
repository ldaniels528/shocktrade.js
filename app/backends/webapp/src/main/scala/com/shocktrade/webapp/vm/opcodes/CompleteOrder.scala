package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.models.contest.OrderOutcome

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
 * Complete Order OpCode
 * @param orderID the given order ID
 */
case class CompleteOrder(orderID: String,
                         fulfilled: Boolean,
                         negotiatedPrice: js.UndefOr[Double] = js.undefined,
                         message: UndefOr[String] = js.undefined) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[OrderOutcome] = {
    try ctx.completeOrder(orderID, fulfilled, negotiatedPrice, message) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "orderID" -> orderID,
    "fulfilled" -> fulfilled,
    "negotiatedPrice" -> negotiatedPrice,
    "message" -> message
  )

}