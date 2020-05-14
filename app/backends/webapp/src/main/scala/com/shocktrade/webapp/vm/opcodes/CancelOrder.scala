package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.contest.OrderOutcome
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Represents a Cancel Order operation
 * @param orderID the given order ID
 */
case class CancelOrder(orderID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[OrderOutcome] = {
    try ctx.cancelOrder(orderID) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("orderID" -> orderID)

}

/**
 * Cancel Order Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CancelOrder extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CancelOrder] = index.orderID.map(CancelOrder.apply)

}