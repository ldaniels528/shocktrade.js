package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * OpCode Error
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class OpCodeError(message: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Any] = {
    Future.failed(js.JavaScriptException(message))
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("message" -> message)

}

/**
 * OpCode Error Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OpCodeError extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[OpCodeError] = {
    for {
      message <- index.getAs[String]("message")
    } yield OpCodeError(message)
  }

}