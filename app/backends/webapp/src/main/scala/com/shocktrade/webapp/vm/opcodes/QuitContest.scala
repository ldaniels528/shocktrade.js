package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}

/**
 * Quit Contest OpCode
 * @param contestID the given contest ID
 * @param userID    the given user ID
 */
case class QuitContest(contestID: String, userID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Double] = {
    ctx.quitContest(contestID, userID)
  }

  override def toString = s"${getClass.getSimpleName}(contestID: $contestID, userID: $userID)"

}
