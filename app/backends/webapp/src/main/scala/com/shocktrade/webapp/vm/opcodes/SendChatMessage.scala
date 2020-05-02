package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Send Chat Message OpCode
 * @param contestID the given contest ID
 * @param userID    the given user ID
 * @param message   the given message
 */
case class SendChatMessage(contestID: String, userID: String, message: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.sendChatMessage(contestID, userID, message)
  }

  override def toString = s"""${getClass.getSimpleName}(contestID: $contestID, userID: $userID, message: "$message")"""

}
