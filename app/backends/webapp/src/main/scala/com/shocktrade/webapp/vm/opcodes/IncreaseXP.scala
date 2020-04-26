package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}

/**
 * Increase XP OpCode
 * @param portfolioID the given portfolio ID
 * @param xp          the given experience
 */
case class IncreaseXP(portfolioID: String, xp: Int) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.increaseXP(portfolioID, xp)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, xp: $xp)"
}
