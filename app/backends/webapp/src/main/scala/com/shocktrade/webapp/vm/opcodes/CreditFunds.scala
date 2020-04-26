package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Credit Funds OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount
 */
class CreditFunds(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.creditFunds(portfolioID, amount)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, amount: $amount)"
}
