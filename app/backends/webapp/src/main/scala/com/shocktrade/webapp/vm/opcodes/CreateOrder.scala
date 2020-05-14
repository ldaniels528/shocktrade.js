package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.contest.OrderRef
import com.shocktrade.webapp.routes.contest.dao.OrderData
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Create Order
 * @param portfolioID the given portfolio ID
 * @param order       the given [[OrderData]]
 */
case class CreateOrder(portfolioID: String, order: OrderData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[OrderRef] = {
    try ctx.createOrder(portfolioID, order) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties(
    "portfolioID" -> portfolioID,
    "order" -> order,
    "symbol" -> order.symbol,
    "exchange" -> order.exchange,
    "orderType" -> order.orderType,
    "priceType" -> order.priceType,
    "price" -> order.price,
    "quantity" -> order.quantity
  )

}

/**
 * Create Order Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreateOrder extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreateOrder] = {
    for {
      portfolioID <- index.portfolioID
      order <- index.getAs[OrderData]("order")
    } yield CreateOrder(portfolioID, order)
  }

}