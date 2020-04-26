package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}

/**
 * Join Contest OpCode
 * @param contestID the given contest ID
 * @param userID    the given user ID
 */
case class JoinContest(contestID: String, userID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    import com.shocktrade.webapp.routes.dao._
    contestDAO.join(contestID, userID)
  }

  override def toString = s"${getClass.getSimpleName}(contestID: $contestID, userID: $userID)"

}
