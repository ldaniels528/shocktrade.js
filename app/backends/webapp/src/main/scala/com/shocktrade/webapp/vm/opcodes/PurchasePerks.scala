package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.models.contest.PurchasePerksResponse
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Purchase Perks
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PurchasePerks(portfolioID: String, perkCodes: js.Array[String]) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PurchasePerksResponse] = {
   try ctx.purchasePerks(portfolioID, perkCodes) catch {
     case e: Exception =>
       Future.failed(e)
   }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties(
    "portfolioID" -> portfolioID,
    "perkCodes" -> perkCodes
  )

}

/**
 * Purchase Perks Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PurchasePerks extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[PurchasePerks] = {
    for {
      portfolioID <- index.portfolioID
      perkCodes <- index.getAs[js.Array[String]]("perkCodes")
    } yield PurchasePerks(portfolioID, perkCodes)
  }

}