package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.routes.account.dao.UserAccountData
import com.shocktrade.webapp.vm.VirtualMachineContext
import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}

case class CreateUserAccount(account: UserAccountData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.createUserAccount(account)
  }

  override def toString: String = s"${getClass.getSimpleName}(${JSON.stringify(account)})"

}