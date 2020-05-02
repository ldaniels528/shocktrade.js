package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Debit Wallet OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount
 */
case class DebitWallet(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Double] = {
    ctx.debitWallet(portfolioID, amount)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, amount: $amount)"
}
