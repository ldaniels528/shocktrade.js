package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

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

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties(
    "contestID" -> request.contestID,
    "userID" -> request.userID,
    "request" -> request
  )

}

/**
 * Create Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreateContest extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreateContest] = {
    for {request <- index.getAs[ContestCreationRequest]("request")} yield CreateContest(request)
  }

}