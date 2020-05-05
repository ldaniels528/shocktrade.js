package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.user.UserRef
import com.shocktrade.webapp.routes.account.dao.UserAccountData
import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}

case class CreateUserAccount(account: UserAccountData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[UserRef] = {
    try ctx.createUserAccount(account) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex("account" -> account)

}