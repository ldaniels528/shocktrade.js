package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.routes.account.dao.UserIconData
import com.shocktrade.webapp.vm.VirtualMachineContext
import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}

case class CreateIcon(icon: UserIconData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.createIcon(icon)
  }

  override def toString: String = s"${getClass.getSimpleName}(${JSON.stringify(icon)})"

}