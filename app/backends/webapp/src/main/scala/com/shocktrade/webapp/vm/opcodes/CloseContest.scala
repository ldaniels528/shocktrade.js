package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.Ok
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

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

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("contestID" -> contestID)

}

/**
 * Close Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CloseContest extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CloseContest] = index.contestID.map(CloseContest.apply)

}
