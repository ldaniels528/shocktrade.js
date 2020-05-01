package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

class CancelOrder(orderID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.cancelOrder(orderID)
  }

  override def toString: String = s"${getClass.getSimpleName}(orderID: $orderID)"

}