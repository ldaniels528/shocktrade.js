package com.shocktrade.webapp.vm
package opcodes

import com.shocktrade.webapp.vm.dao.ClosePortfolioResponse
import com.shocktrade.webapp.vm.opcodes.OpCode.OpCodeCompiler

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Liquidate Portfolio OpCode
 * @param portfolioID the given portfolio ID
 */
case class LiquidatePortfolio(portfolioID: String) extends OpCode {

  override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[ClosePortfolioResponse] = {
    try ctx.liquidatePortfolio(portfolioID) catch {
      case e: Exception =>
        Future.failed(e)
    }
  }

  override val decompile: OpCodeProperties = super.decompile ++ OpCodeProperties("portfolioID" -> portfolioID)

}

/**
 * Liquidate Portfolio Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object LiquidatePortfolio extends OpCodeCompiler {

  override def compile(index: OpCodeProperties): js.UndefOr[LiquidatePortfolio] = {
    for {
      portfolioID <- index.portfolioID
    } yield LiquidatePortfolio(portfolioID)
  }

}
