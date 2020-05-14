package com.shocktrade.webapp.vm
package opcodes

import io.scalajs.JSON
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Represents Contest Virtual Machine (CVM) Operational code
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OpCode {

  def decompile: OpCodeProperties = OpCodeProperties("type" -> getClass.getSimpleName)

  def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Any]

  override def toString: String = {
    val index = decompile
    (for {
      name <- index.`type`
      filteredProps = js.Dictionary(index.asInstanceOf[js.Dictionary[_]].toSeq.filterNot(_._1 == "type"): _*)
    } yield s"$name(${JSON.stringify(filteredProps)})") getOrElse JSON.stringify(index)
  }

}

/**
 * OpCode Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OpCode {
  private val compilers = js.Dictionary[OpCodeCompiler](Seq(
    CancelOrder, CloseContest, CompleteOrder, CreateContest, CreateOrder, CreateUserAccount, CreateUserIcon,
    CreditWallet, DebitWallet, DecreasePosition, IncreasePosition, JoinContest, LiquidatePortfolio, OpCodeError,
    PurchasePerks
  ).map(t => t.getClass.getSimpleName -> t): _*)

  def compile(props: OpCodeProperties): js.UndefOr[OpCode] = {
    for {
      name <- props.`type`.flat
      compiler <- compilers.get(name).orUndefined
      opCode = compiler.compile(props) getOrElse OpCodeError(s"OpCode '$name' not found")
    } yield opCode
  }

  /**
   * OpCode Callback
   * @param opCode the given [[OpCode]]
   * @param promise the given [[Promise]]
   */
  case class Callback(opCode: OpCode, promise: Promise[js.Any]) extends OpCode {

    override val decompile: OpCodeProperties = opCode.decompile

    override def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Any] = {
      val outcome = opCode.invoke()
      outcome onComplete {
        case Success(value) => promise.success(value.asInstanceOf[js.Any])
        case Failure(e) => promise.failure(e)
      }
      outcome
    }

    override def toString: String = opCode.toString

  }

  /**
   * OpCode Compiler Support
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  trait OpCodeCompiler {

    def compile(index: OpCodeProperties): js.UndefOr[OpCode]

  }

}
