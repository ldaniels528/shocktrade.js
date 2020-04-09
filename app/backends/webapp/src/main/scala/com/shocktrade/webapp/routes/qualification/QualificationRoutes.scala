package com.shocktrade.webapp.routes.qualification

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.server.common.TradingClock
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.contest.dao.{OrderData, PositionData}
import com.shocktrade.webapp.routes.qualification.QualificationRoutes.CqmResponse
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
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
      case Success((contestRefs, positions, updatedOrders, positionCount, rejectedCount)) =>
        response.send(new CqmResponse(
          contestRefs = contestRefs,
          positions = positions.toJSArray,
          updatedOrders = updatedOrders.toJSArray,
          positionCount = positionCount,
          updatedOrderCount = rejectedCount
        ))
        next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}

/**
 * Qualification Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QualificationRoutes {

  class CqmResponse(val contestRefs: js.Array[ContestRef],
                    val positions: js.UndefOr[js.Array[PositionData]],
                    val updatedOrders: js.UndefOr[js.Array[OrderData]],
                    val positionCount: js.UndefOr[Int],
                    val updatedOrderCount: js.UndefOr[Int]) extends js.Object

}