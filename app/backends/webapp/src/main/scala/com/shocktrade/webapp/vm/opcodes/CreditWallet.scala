package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Credit Wallet OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount
 */
case class CreditWallet(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Double] = {
    try ctx.creditWallet(portfolioID, amount) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("portfolioID" -> portfolioID, "amount" -> amount)

}
