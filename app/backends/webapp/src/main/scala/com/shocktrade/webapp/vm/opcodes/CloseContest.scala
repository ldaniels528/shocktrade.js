package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.dao.ClosePortfolioResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Close Contest OpCode
 * @param contestID the given contest ID
 */
case class CloseContest(contestID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Array[ClosePortfolioResponse]] = {
    try ctx.closeContest(contestID) catch {
      case e: Exception => Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("contestID" -> contestID)

}
