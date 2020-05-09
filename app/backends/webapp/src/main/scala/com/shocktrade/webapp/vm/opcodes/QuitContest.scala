package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity

import scala.concurrent.{ExecutionContext, Future}

/**
 * Quit Contest OpCode
 * @param contestID the given contest ID
 * @param userID    the given user ID
 */
case class QuitContest(contestID: String, userID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PortfolioEquity] = {
    try ctx.quitContest(contestID, userID) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "contestID" -> contestID,
    "userID" -> userID
  )

}
