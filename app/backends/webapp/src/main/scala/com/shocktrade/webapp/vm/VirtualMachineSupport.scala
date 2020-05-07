package com.shocktrade.webapp.vm

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO
import io.scalajs.nodejs._
import io.scalajs.nodejs.timers.Interval
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Virtual Machine Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait VirtualMachineSupport {
  private val logger = LoggerFactory.getLogger(getClass)
  private var isAlive: Boolean = false
  private var lifeCycleHandle: Option[Interval] = None
  private var updateEventHandle: Option[Interval] = None

  /**
   * Indicates whether the virtual machine is running
   * @return true, if the virtual machine is running
   */
  def isRunning: Boolean = isAlive

  /**
   * Starts the virtual machine
   * @param ec     the implicit [[ExecutionContext]]
   * @param cqm    the implicit [[ContestQualificationModule]]
   * @param cqmDAO the implicit [[QualificationDAO]]
   * @param vm     the implicit [[VirtualMachine]]
   * @param ctx    the implicit [[VirtualMachineContext]]
   */
  def startMachine()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    if (!isAlive) {
      isAlive = true

      // queue the CQM life-cycle updates
      lifeCycleHandle = Option(setInterval(() => updateContestLifeCycles(), 30.seconds))

      // keep the event log up-to-date
      updateEventHandle = Option(setInterval(() => updateEventLog(), 1.minutes))

      // start the virtual CPU
      continuouslyConsumeOpCodes()
    }
  }

  /**
   * Stops the virtual machine
   */
  def stopMachine(): Unit = {
    isAlive = false
    lifeCycleHandle.foreach(_.clear())
    lifeCycleHandle = None
    updateEventHandle.foreach(_.clear())
    updateEventHandle = None
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Processing Methods
  //////////////////////////////////////////////////////////////////////////////////////

  private def continuouslyConsumeOpCodes()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    Future(drainPipeline()) onComplete { _ => if (isAlive) setTimeout(() => continuouslyConsumeOpCodes(), 5.millis) }
  }

  private def drainPipeline()(implicit ec: ExecutionContext, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    try {
      val startTime = js.Date.now()
      vm.invokeAll() onComplete {
        case Success(results) =>
          val elapsedTime = js.Date.now() - startTime
          val count = results.length
          if (count > 0) logger.info(f"$count opCodes executed in $elapsedTime msec [${elapsedTime / count}%.1f ops/msec]")
          results foreach { case VmProcess(code, result, runTime) =>
            logger.info(s"[${runTime.toMillis} ms] $code => ${VirtualMachine.unwrap(result)}")
          }
        case Failure(e) =>
          val elapsedTime = js.Date.now() - startTime
          logger.error(s"drainPipeline| ${e.getMessage} [$elapsedTime msec]")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unexpected error during Pipeline processing: ${e.getMessage}")
    }
  }

  private def updateContestLifeCycles()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    try {
      val startTime = js.Date.now()
      cqm.run() onComplete {
        case Success(opCodes) =>
          val elapsedTime = js.Date.now() - startTime
          val count = opCodes.length
          if (count > 0) {
            vm.enqueue(opCodes: _*)
            logger.info(s"$count opCodes scheduled in $elapsedTime msec")
          }
        case Failure(e) =>
          logger.error(s"updateContestLifeCycles| ${e.getMessage}")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unexpected error during Contest Life-cycle: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def updateEventLog()(implicit  ctx: VirtualMachineContext, ec: ExecutionContext): Unit = {
    val startTime = System.currentTimeMillis()
    ctx.updateEventLog() onComplete {
      case Success(result) =>
        val count = result.updateCount.orZero
        if (count > 0) {
          val elapsedTime = System.currentTimeMillis() - startTime
          logger.info(s"Indexed $count events in $elapsedTime msec")
        }
      case Failure(e) =>
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info(s"Failed updating events after $elapsedTime msec: ${e.getMessage}")
    }
  }

}
