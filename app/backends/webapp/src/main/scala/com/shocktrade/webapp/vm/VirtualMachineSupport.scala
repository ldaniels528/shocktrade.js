package com.shocktrade.webapp.vm

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Virtual Machine Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait VirtualMachineSupport {
  private val logger = LoggerFactory.getLogger(getClass)

  //////////////////////////////////////////////////////////////////////////////////////
  //      Processing Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def drainPipeline()(implicit ec: ExecutionContext, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    try {
      val startTime = js.Date.now()
      vm.invokeAll() onComplete {
        case Success(results) =>
          val elapsedTime = js.Date.now() - startTime
          val count = results.length
          if (count > 0) logger.info(f"$count opCodes executed in $elapsedTime msec [${elapsedTime / count}%.1f ops/msec]")
          results foreach { case VmProcess(code, result, runTime) =>
            logger.info(s"[${runTime.toMillis} ms] $code => ${result.orNull}")
          }
        case Failure(e) =>
          logger.error(s"PIPELINE| ${e.getMessage}")
          e.printStackTrace()
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unexpected error during Pipeline processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  def updateContestLifeCycles()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
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
          logger.error(s"LIFECYCLE| ${e.getMessage}")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unexpected error during Contest Life-cycle: ${e.getMessage}")
        e.printStackTrace()
    }
  }

}
