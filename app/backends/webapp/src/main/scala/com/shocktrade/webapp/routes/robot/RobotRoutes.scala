package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.Ok
import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.robot.dao.RobotDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Robot Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotRoutes(app: Application)(implicit ec: ExecutionContext, robotDAO: RobotDAO) {
  // API routes
  app.get("/api/robot/:name", (request: Request, response: Response, next: NextFunction) => findRobot(request, response, next))
  app.get("/api/robot/:name/fomo", (request: Request, response: Response, next: NextFunction) => findContestsToJoin(request, response, next))
  app.get("/api/robot/:name/off", (request: Request, response: Response, next: NextFunction) => toggleRobot(request, response, next, isActive = false))
  app.get("/api/robot/:name/on", (request: Request, response: Response, next: NextFunction) => toggleRobot(request, response, next, isActive = true))
  app.get("/api/robots", (request: Request, response: Response, next: NextFunction) => findRobots(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def findContestsToJoin(request: Request, response: Response, next: NextFunction): Unit = {
    val username = request.params("name")
    robotDAO.findContestsToJoin(username, limit = 20) onComplete {
      case Success(contestRefs) => response.send(contestRefs); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def findRobot(request: Request, response: Response, next: NextFunction): Unit = {
    val username = request.params("name")
    robotDAO.findRobot(username) onComplete {
      case Success(robots) => response.send(robots); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def findRobots(request: Request, response: Response, next: NextFunction): Unit = {
    robotDAO.findRobots onComplete {
      case Success(robots) => response.send(robots); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def toggleRobot(request: Request, response: Response, next: NextFunction, isActive: Boolean): Unit = {
    val username = request.params("name")
    robotDAO.toggleRobot(username, isActive) onComplete {
      case Success(count) => response.send(Ok(count)); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
