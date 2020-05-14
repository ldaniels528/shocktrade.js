package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.user.UserRef
import com.shocktrade.webapp.routes.account.dao.UserAccountData
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Create User Account
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class CreateUserAccount(account: UserAccountData) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[UserRef] = {
    try ctx.createUserAccount(account) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("account" -> account)

}

/**
 * Create User Account Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreateUserAccount extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreateUserAccount] = {
    for {
      account <- index.getAs[UserAccountData]("account")
    } yield CreateUserAccount(account)
  }

}