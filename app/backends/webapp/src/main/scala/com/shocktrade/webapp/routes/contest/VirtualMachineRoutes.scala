package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO
import com.shocktrade.webapp.vm.{VirtualMachine, VirtualMachineSupport}
import io.scalajs.nodejs._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * Virtual Machine Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachineRoutes(app: Application)(implicit ec: ExecutionContext, cqmDAO: QualificationDAO, cqm: ContestQualificationModule, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
extends VirtualMachineSupport {

  // API routes
  app.get("/api/vm/queued", (request: Request, response: Response, next: NextFunction) => queued(request, response, next))
  app.get("/api/vm/run", (request: Request, response: Response, next: NextFunction) => run(request, response, next))

  // queue the CQM life-cycle updates
  setImmediate(() => updateContestLifeCycles())
  setInterval(() => updateContestLifeCycles(), 30.seconds)

  // process all queued opCodes
  setInterval(() => drainPipeline(), 5.seconds)

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def queued(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(vm.queued)
    next()
  }

  def run(request: Request, response: Response, next: NextFunction): Unit = {
    vm.invokeAll() onComplete {
      case Success(results) => response.send(Ok(results.length)); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
