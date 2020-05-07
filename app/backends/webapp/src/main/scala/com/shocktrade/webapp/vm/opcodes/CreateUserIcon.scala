package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.Ok
import com.shocktrade.webapp.routes.account.dao.UserIconData
import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

case class CreateUserIcon(icon: UserIconData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    try ctx.createUserIcon(icon) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("icon" -> icon)

}