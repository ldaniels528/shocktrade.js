package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.Ok

import scala.concurrent.{ExecutionContext, Future}

/**
 * Close Contest OpCode
 * @param contestID the given contest ID
 */
case class CloseContest(contestID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    try ctx.closeContest(contestID) catch {
      case e: Exception => Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("contestID" -> contestID)

}
