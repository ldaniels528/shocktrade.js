package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.common.models.contest.PortfolioRef
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

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

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties(
    "contestID" -> contestID,
    "userID" -> userID
  )

}

/**
 * Join Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object JoinContest extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[JoinContest] = {
    for {
      contestID <- index.contestID
      userID <- index.userID
    } yield JoinContest(contestID, userID)
  }

}