package com.shocktrade.webapp.vm.opcodes

import com.shocktrade.webapp.vm.VirtualMachineContext
import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Debit Wallet OpCode
 * @param portfolioID the given portfolio ID
 * @param amount      the given amount
 */
case class DebitWallet(portfolioID: String, amount: Double) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[PortfolioEquity] = {
    try ctx.debitWallet(portfolioID, amount) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("portfolioID" -> portfolioID, "amount" -> amount)

}

/**
 * Debit Wallet Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object DebitWallet extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[DebitWallet] = {
    for {
      portfolioID <- index.portfolioID
      amount <- index.getAs[Double]("amount")
    } yield DebitWallet(portfolioID, amount)
  }

}
