package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
 * Complete Order OpCode
 * @param orderID the given order ID
 */
case class CompleteOrder(orderID: String, fulfilled: Boolean, message: UndefOr[String] = js.undefined) extends OpCode {
  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.completeOrder(orderID, fulfilled, message)
  }

  override def toString: String = s"${getClass.getSimpleName}(orderID: $orderID, fulfilled: $fulfilled, message: '${message.orNull}')"
}