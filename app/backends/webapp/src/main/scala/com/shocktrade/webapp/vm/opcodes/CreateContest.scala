package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Create Contest OpCode
 * @param request the given [[ContestCreationRequest]]
 */
case class CreateContest(request: ContestCreationRequest) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[ContestCreationResponse] = {
    try ctx.createContest(request) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "contestID" -> request.contestID,
    "userID" -> request.userID,
    "request" -> request
  )

}
