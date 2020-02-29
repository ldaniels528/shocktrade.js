package com.shocktrade.ingestion.routes

import com.shocktrade.ingestion.daemons.cqm.ContestQualificationModule
import com.shocktrade.ingestion.routes.QualificationRoutes.CqmResponse
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Qualification Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QualificationRoutes(app: Application, cqm: ContestQualificationModule)(implicit clock: TradingClock, ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  app.get("/api/cqm/run", (request: Request, response: Response, next: NextFunction) => runCQM(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def runCQM(request: Request, response: Response, next: NextFunction): Unit = {
    val marketClosed = request.query.get("marketClosed").contains("true")
    logger.info("Starting the Contest Qualification Module....")
    cqm.execute(marketClosed) onComplete {
      case Success(count) =>
        logger.info(s"CQM: $count order(s) updated")
        response.send(new CqmResponse(count))
        next()
      case Failure(e) =>
        e.printStackTrace()
        response.internalServerError(e)
        next()
    }
  }

}

/**
 * Qualification Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QualificationRoutes {

  class CqmResponse(val count: Int) extends js.Object

}
