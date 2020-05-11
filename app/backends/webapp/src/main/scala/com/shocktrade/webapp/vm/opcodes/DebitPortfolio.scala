package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity

import scala.concurrent.{ExecutionContext, Future}

/**
 * Debit Portfolio OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount to debit
 */
case class DebitPortfolio(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PortfolioEquity] = {
    try ctx.debitPortfolio(portfolioID, amount) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "portfolioID" -> portfolioID,
    "amount" -> amount
  )

}
