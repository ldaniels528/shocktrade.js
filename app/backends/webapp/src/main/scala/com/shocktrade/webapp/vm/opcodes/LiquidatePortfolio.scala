package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity

import scala.concurrent.{ExecutionContext, Future}

/**
 * Liquidate Portfolio OpCode
 * @param portfolioID the given portfolio ID
 */
case class LiquidatePortfolio(portfolioID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PortfolioEquity] = {
    try ctx.liquidatePortfolio(portfolioID) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("portfolioID" -> portfolioID)

}
