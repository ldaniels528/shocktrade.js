package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Grant Award
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class GrantAward(portfolioID: String, awardCode: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Any] = {
    ctx.grantAward(portfolioID, awardCode)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, awardCode: $awardCode)"

}
