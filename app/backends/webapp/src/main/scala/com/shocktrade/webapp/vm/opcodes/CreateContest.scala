package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}

/**
 * Create Contest OpCode
 * @param request the given [[ContestCreationRequest]]
 */
case class CreateContest(request: ContestCreationRequest) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[ContestCreationResponse] = {
    import com.shocktrade.webapp.routes.dao._
    contestDAO.create(request)
  }

  override def toString = s"${getClass.getSimpleName}(${JSON.stringify(request)})"

}
