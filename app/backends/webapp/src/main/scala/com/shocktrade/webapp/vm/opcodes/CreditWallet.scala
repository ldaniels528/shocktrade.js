package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.common.Ok
import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Credit Wallet OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount
 */
case class CreditWallet(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    try ctx.creditWallet(portfolioID, amount) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("portfolioID" -> portfolioID, "amount" -> amount)

}

/**
 * Credit Wallet Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CreditWallet extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[CreditWallet] = {
    for {
      portfolioID <- index.portfolioID
      amount <- index.getAs[Double]("amount")
    } yield CreditWallet(portfolioID, amount)
  }

}