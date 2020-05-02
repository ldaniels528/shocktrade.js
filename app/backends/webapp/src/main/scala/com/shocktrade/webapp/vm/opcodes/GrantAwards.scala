package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Grant Awards
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class GrantAwards(portfolioID: String, awardCodes: js.Array[String]) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Any] = {
    ctx.grantAwards(portfolioID, awardCodes)
  }

  override def toString = s"${getClass.getSimpleName}(portfolioID: $portfolioID, awardCode: [${awardCodes.mkString(",")}])"

}
