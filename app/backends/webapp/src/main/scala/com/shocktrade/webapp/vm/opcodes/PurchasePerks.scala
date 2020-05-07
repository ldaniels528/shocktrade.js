package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.contest.PurchasePerksResponse
import com.shocktrade.webapp.vm.VirtualMachineContext

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

case class PurchasePerks(portfolioID: String, perkCodes: js.Array[String]) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PurchasePerksResponse] = {
   try ctx.purchasePerks(portfolioID, perkCodes) catch {
     case e: Exception =>
       Future.failed(e)
   }
  }

  override val toJsObject: EventSourceIndex = super.toJsObject ++ EventSourceIndex(
    "portfolioID" -> portfolioID,
    "perkCodes" -> perkCodes
  )

}