package com.shocktrade.webapp.routes.qualification

import com.shocktrade.server.common.TradingClock
import com.shocktrade.webapp.routes._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * Qualification Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QualificationRoutes(app: Application)(implicit ec: ExecutionContext, cqm: ContestQualificationModule, clock: TradingClock) {

  // API routes
  app.get("/api/cqm/run", (request: Request, response: Response, next: NextFunction) => runCQM(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def runCQM(request: Request, response: Response, next: NextFunction): Unit = {
    cqm.start() onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
