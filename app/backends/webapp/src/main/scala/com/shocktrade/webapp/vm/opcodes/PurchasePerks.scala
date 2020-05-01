package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class PurchasePerks(portfolioID: String, perkCodes: js.Array[String]) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Int] = {
    ctx.purchasePerks(portfolioID, perkCodes)
  }

  override def toString: String = s"${getClass.getSimpleName}(portfolioID: $portfolioID, perkCodes: $perkCodes)"

}