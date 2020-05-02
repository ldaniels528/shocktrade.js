package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.routes.contest.dao.OrderData
import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.JSON

case class CreateOrder(portfolioID: String, order: OrderData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.createOrder(portfolioID, order)
  }

  override def toString: String = s"${getClass.getSimpleName}(portfolioID: $portfolioID, order: ${JSON.stringify(order)})"
}