package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.models.contest.OrderOutcome
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

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

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties(
    "orderID" -> orderID,
    "fulfilled" -> fulfilled,
    "negotiatedPrice" -> negotiatedPrice,
    "message" -> message
  )

}

/**
 * Complete Order Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CompleteOrder extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CompleteOrder] = {
    for {
      orderID <- index.orderID
      fulfilled <- index.getAs[Boolean]("fulfilled")
      negotiatedPrice <- index.getAs[Double]("negotiatedPrice")
      message <- index.getAs[String]("message")
    } yield CompleteOrder(orderID, fulfilled, negotiatedPrice, message)
  }

}