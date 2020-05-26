package com.shocktrade.webapp.vm

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.traffic.TrackingDAO
import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO
import io.scalajs.nodejs._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Virtual Machine Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait VirtualMachineSupport {
  private val logger = LoggerFactory.getLogger(getClass)
  private var isAlive: Boolean = false

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
  def startMachine()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, trackingDAO: TrackingDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    if (!isAlive) {
      isAlive = true

      // start the virtual CPU and CQM
      startCPU()
      startCQM()
    }
  }

  /**
   * Stops the virtual machine
   */
  def stopMachine(): Unit = {
    isAlive = false
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Processing Methods
  //////////////////////////////////////////////////////////////////////////////////////

  private def consumeOpCodes()(implicit ec: ExecutionContext, trackingDAO: TrackingDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Future[js.Array[VmProcess]] = {
    val startTime = js.Date.now()
    val outcome = vm.invokeAll()
    outcome onComplete {
      case Success(results) =>
        val elapsedTime = js.Date.now() - startTime
        val count = results.length
        if (count > 0) logger.info(f"$count opCodes executed in $elapsedTime msec [${count * 1000.0 / elapsedTime}%.1f ops/sec]")
        val codeTypes = Set("LiquidatePortfolio")
        results foreach {
          case VmProcess(code, result, runTime) if code.decompile.`type`.exists(codeTypes.contains) =>
            logger.info(s"[${runTime.toMillis} ms] $code => ${VirtualMachine.unwrap(result)}")
          case _ =>
        }
      case Failure(e) =>
        val elapsedTime = js.Date.now() - startTime
        logger.error(s"consumeOpCodes| ${e.getMessage} [$elapsedTime msec]")
    }
    outcome
  }

  private def startCPU()(implicit ec: ExecutionContext, trackingDAO: TrackingDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    consumeOpCodes() onComplete { _ => if (isAlive) setTimeout(() => startCPU(), 0.millis) }
  }

  private def startCQM()(implicit ec: ExecutionContext, cqm: ContestQualificationModule, cqmDAO: QualificationDAO, vm: VirtualMachine, ctx: VirtualMachineContext): Unit = {
    (for {
      opCodes <- cqm.run()
      responses <- Future.sequence(opCodes.toSeq.map(vm.invoke(_)))
    } yield responses) onComplete { _ => if (isAlive) setTimeout(() => startCQM(), 5.seconds) }
  }

}
