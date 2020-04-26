package com.shocktrade.webapp.vm

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.opcodes.OpCode
import io.scalajs.nodejs.setImmediate

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Virtual Machine
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachine() {
  private val logger = LoggerFactory.getLogger(getClass)
  private val pipeline = js.Array[OpCode]()

  /**
   * Executes all queued opCodes in the pipeline
   * @param ctx     the implicit [[VirtualMachineContext]]
   * @param ec      the implicit [[ExecutionContext]]
   * @return the cumulative invocation result
   */
  def invokeAll()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[List[VmProcess]] = {
    if(pipeline.isEmpty) Future.successful(Nil)
    else {
      // copy the opCodes into our processing array
      val opCodes = new Array[OpCode](pipeline.size)
      pipeline.copyToArray(opCodes)
      pipeline.clear()

      // execute the opCodes
      waterfall(opCodes)
    }
  }

  def waterfall(opCodes: Seq[OpCode])(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[List[VmProcess]] = {
    val promise = Promise[List[VmProcess]]()
    var results: List[VmProcess] = Nil

    def recurse(codes: List[OpCode]): Unit = {
      try {
        codes match {
          case code :: codes =>
            val startTime = js.Date.now()
            code.invoke() onComplete {
              case Success(result) => results = VmProcess(code, result, (js.Date.now() - startTime).millis) :: results; recurse(codes)
              case Failure(e) => promise.failure(e)
            }
          case Nil => promise.success(results.reverse)
        }
      } catch {
        case e: Exception => promise.failure(e)
      }
    }

    setImmediate(() => recurse(opCodes.toList))
    promise.future
  }

  /**
   * Executes the next queued opCode from the pipeline
   * @param ctx the implicit [[VirtualMachineContext]]
   * @param ec  the implicit [[ExecutionContext]]
   * @return the option of an invocation result
   */
  def invokeNext()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Option[Future[Any]] = {
    if (pipeline.isEmpty) None else {
      val opCode = pipeline.pop()
      Option(opCode.invoke())
    }
  }

  /**
   * Queues opCodes for processing
   * @param opCodes one or more [[OpCode]]
   * @return the number of opCodes queued
   */
  def enqueue(opCodes: OpCode*): Int = pipeline.push(opCodes: _*)

  /**
   * Returns the collection queued OpCodes
   * @return the collection of OpCodes
   */
  def queued: js.Array[String] = pipeline.map(_.toString)

}

/**
 * Virtual Machine Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object VirtualMachine {

  case class VmProcess(code: OpCode, result: js.UndefOr[Any], runTime: FiniteDuration) {
    override def toString: String = s"${getClass.getSimpleName}(code: $code, result: ${result.orNull}, $runTime)"
  }

}