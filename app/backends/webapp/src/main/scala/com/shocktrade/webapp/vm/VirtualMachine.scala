package com.shocktrade.webapp.vm

import com.shocktrade.common.Ok
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.dao._
import com.shocktrade.webapp.vm.opcodes.{OpCode, OpCodeProperties}
import io.scalajs.JSON
import io.scalajs.nodejs.setImmediate
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Virtual Machine
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachine() {
  private val pipeline = js.Array[OpCode]()

  /**
   * Executes the next queued opCode from the pipeline
   * @param ctx the implicit [[VirtualMachineContext]]
   * @return the promise of an invocation result
   */
  def invoke(opCode: OpCode)(implicit ctx: VirtualMachineContext): Future[js.Any] = {
    val promise = Promise[js.Any]()
    pipeline.push(OpCode.Callback(opCode, promise))
    promise.future
  }

  /**
   * Executes all queued opCodes in the pipeline
   * @param ctx     the implicit [[VirtualMachineContext]]
   * @param ec      the implicit [[ExecutionContext]]
   * @return the cumulative invocation result
   */
  def invokeAll()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Array[VmProcess]] = {
    if(pipeline.isEmpty) Future.successful(js.Array())
    else {
      // copy the opCodes into our processing array
      val opCodes = new Array[OpCode](pipeline.size)
      pipeline.copyToArray(opCodes)
      pipeline.clear()

      // execute the opCodes
      VirtualMachine.waterfall(opCodes)
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
  private val logger = LoggerFactory.getLogger(getClass)

  def waterfall(opCodes: Seq[OpCode])(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[js.Array[VmProcess]] = {
    val promise = Promise[js.Array[VmProcess]]()
    var results: List[VmProcess] = Nil

    def recurse(codes: List[OpCode]): Unit = {
        codes match {
          case code :: codes =>
            try {
              val startTime = js.Date.now()
              code.invoke() onComplete {
                // track this successful completion
                case Success(result) =>
                  val elapsedTime = js.Date.now() - startTime
                  results = VmProcess(code, result, elapsedTime.millis) :: results
                  trackEvent(request = code, requestedTime = startTime, response = unwrap(result), responseTimeMillis = elapsedTime, failed = false)
                  recurse(codes)
                // track this failure
                case Failure(e) =>
                  val elapsedTime = js.Date.now() - startTime
                  trackEvent(request = code, requestedTime = startTime, response = e.getMessage, responseTimeMillis = elapsedTime, failed = true)
                  recurse(codes)
              }
            } catch {
              case e: Exception =>
                logger.error(s"OpCode failed: ${e.getMessage} ~> ${code.toString}")
            }
          case Nil => promise.success(results.reverse.toJSArray)
        }
    }

    setImmediate(() => recurse(opCodes.toList))
    promise.future
  }

  private def trackEvent(request: OpCode,
                         requestedTime: Double,
                         response: String,
                         responseTimeMillis: Double,
                         failed: Boolean)(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Ok] = {
    val requestProps = request.decompile
    val responseProps = if (response.startsWith("{") && response.endsWith("}")) JSON.parseAs[js.UndefOr[OpCodeProperties]](response) else js.undefined
    val outcome = ctx.trackEvent(EventSourceData(
      command = JSON.stringify(requestProps),
      `type` = requestProps.`type`,
      contestID = requestProps.contestID ?? responseProps.flatMap(_.contestID),
      portfolioID = requestProps.portfolioID ?? responseProps.flatMap(_.portfolioID),
      positionID = requestProps.positionID ?? responseProps.flatMap(_.positionID),
      userID = requestProps.userID ?? responseProps.flatMap(_.userID),
      orderID = requestProps.orderID ?? responseProps.flatMap(_.orderID),
      symbol = requestProps.symbol ?? responseProps.flatMap(_.symbol),
      exchange = requestProps.exchange ?? responseProps.flatMap(_.exchange),
      orderType = requestProps.orderType ?? responseProps.flatMap(_.orderType),
      priceType = requestProps.priceType ?? responseProps.flatMap(_.priceType),
      quantity = requestProps.quantity ?? responseProps.flatMap(_.quantity),
      negotiatedPrice = requestProps.negotiatedPrice ?? responseProps.flatMap(_.negotiatedPrice),
      xp = requestProps.xp ?? responseProps.flatMap(_.xp),
      response = response,
      responseTimeMillis = responseTimeMillis,
      creationTime = new js.Date(requestedTime),
      failed = failed
    ))
    outcome onComplete {
      case Success(_) =>
      case Failure(e) => logger.error(s"Event source error: ${e.getMessage}")
    }
    outcome
  }

  private[vm] def unwrap(result: js.UndefOr[Any]): String = {
    result map {
      case null => "null"
      case o: Option[Any] => o.map(unwrap(_)).getOrElse("null")
      case x if x.toString.startsWith("[object") => JSON.stringify(x.asInstanceOf[js.Any])
      case v => v.toString
    } getOrElse "undefined"
  }

  case class VmProcess(code: OpCode, result: js.UndefOr[Any], runTime: FiniteDuration) {
    override def toString: String = s"${getClass.getSimpleName}(code: $code, result: ${result.orNull}, $runTime)"
  }

}