package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.models.contest.PortfolioRef

import scala.concurrent.{ExecutionContext, Future}

/**
 * Join Contest OpCode
 * @param contestID the given contest ID
 * @param userID    the given user ID
 */
case class JoinContest(contestID: String, userID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PortfolioRef] = {
    try ctx.joinContest(contestID, userID) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "contestID" -> contestID,
    "userID" -> userID
  )

}
