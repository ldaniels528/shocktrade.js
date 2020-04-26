package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}

/**
 * Liquidate Portfolio OpCode
 * @param portfolioID the given portfolio ID
 */
case class LiquidatePortfolio(portfolioID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Any] = {
    ctx.liquidatePortfolio(portfolioID)
  }

  override def toString: String = s"${getClass.getSimpleName}(portfolioID: $portfolioID)"
}
