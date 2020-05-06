package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.Ok
import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

case class CancelOrder(orderID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    try ctx.cancelOrder(orderID).map(Ok(_)) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("orderID" -> orderID)

}