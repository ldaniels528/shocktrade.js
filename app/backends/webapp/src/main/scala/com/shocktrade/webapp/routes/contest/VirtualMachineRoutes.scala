package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO
import com.shocktrade.webapp.vm.{VirtualMachine, VirtualMachineSupport}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
 * Virtual Machine Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachineRoutes(app: Application)(implicit ec: ExecutionContext, cqmDAO: QualificationDAO, cqm: ContestQualificationModule, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
extends VirtualMachineSupport {

  // API routes
  app.get("/api/vm/queued", (request: Request, response: Response, next: NextFunction) => queued(request, response, next))
  app.get("/api/vm/start", (request: Request, response: Response, next: NextFunction) => start(request, response, next))
  app.get("/api/vm/status", (request: Request, response: Response, next: NextFunction) => status(request, response, next))
  app.get("/api/vm/stop", (request: Request, response: Response, next: NextFunction) => stop(request, response, next))

  // start the virtual machine
  startMachine()

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def queued(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(vm.queued)
    next()
  }

  def start(request: Request, response: Response, next: NextFunction): Unit = {
    startMachine()
    response.send(Ok(1))
    next()
  }

  def status(request: Request, response: Response, next: NextFunction): Unit = {
    startMachine()
    response.send(Ok(if (isRunning) 1 else 0))
    next()
  }

  def stop(request: Request, response: Response, next: NextFunction): Unit = {
    stopMachine()
    response.send(Ok(1))
    next()
  }

}
