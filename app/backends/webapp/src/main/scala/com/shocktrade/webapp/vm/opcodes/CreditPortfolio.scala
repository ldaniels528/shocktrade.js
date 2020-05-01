package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Credit Portfolio OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount to credit
 */
class CreditPortfolio(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Double] = {
    ctx.creditPortfolio(portfolioID, amount)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, amount: $amount)"
}
