package com.shocktrade.webapp.vm
package opcodes

import scala.concurrent.{ExecutionContext, Future}

/**
 * Represents Contest Virtual Machine (CVM) Operational code
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OpCode {

  def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Any]

}
