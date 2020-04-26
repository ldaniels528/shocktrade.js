package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

case class OpCodeError(message: String) extends OpCode {
  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Any] = {
    Future.failed(js.JavaScriptException(message))
  }
}
