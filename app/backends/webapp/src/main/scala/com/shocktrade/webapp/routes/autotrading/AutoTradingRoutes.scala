package com.shocktrade.webapp.routes.autotrading

import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.autotrading.dao.RobotDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Auto-Trading Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AutoTradingRoutes(app: Application)(implicit ec: ExecutionContext) {
  private implicit val robotDAO: RobotDAO = RobotDAO()

  // routes
  app.get("/api/robots", (request: Request, response: Response, next: NextFunction) => findRobots(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def findRobots(request: Request, response: Response, next: NextFunction): Unit = {
    robotDAO.findRobots onComplete {
      case Success(robots) => response.send(robots); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
