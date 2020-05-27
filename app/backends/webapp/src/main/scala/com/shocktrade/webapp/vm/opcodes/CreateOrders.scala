package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.contest.OrderRef
import com.shocktrade.webapp.routes.contest.dao.OrderData
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Create Order
 * @param orders the given collection of [[OrderData]]
 */
case class CreateOrders(orders: js.Array[OrderData]) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Array[OrderRef]] = {
    try ctx.createOrders(orders) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("orders" -> orders)

}

/**
 * Create Order Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreateOrders extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreateOrders] = {
    for {
      orders <- index.asInstanceOf[js.Dictionary[js.Array[OrderData]]].get("orders").orUndefined
    } yield CreateOrders(orders)
  }

}