package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.Ok
import com.shocktrade.webapp.routes.account.dao.UserIconData
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Create User Icon
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class CreateUserIcon(icon: UserIconData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    try ctx.createUserIcon(icon) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("userID" -> icon.userID)

}

/**
 * Create User Icon Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreateUserIcon extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreateUserIcon] = {
    for {
      icon <- index.getAs[UserIconData]("icon")
    } yield CreateUserIcon(icon)
  }

}