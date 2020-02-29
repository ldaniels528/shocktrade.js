package com.shocktrade.webapp.routes.account

import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * User Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val userDAO = UserDAO()

  app.get("/api/user/:id", (request: Request, response: Response, next: NextFunction) => userByID(request, response, next))
  app.put("/api/users", (request: Request, response: Response, next: NextFunction) => usersByID(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves a user by ID
   */
  def userByID(request: Request, response: Response, next: NextFunction): Unit = {
    val id = request.params("id")
    userDAO.findByID(id) onComplete {
      case Success(Some(user)) => response.send(user); next()
      case Success(None) => response.notFound(id); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves an array of users by ID
   */
  def usersByID(request: Request, response: Response, next: NextFunction): Unit = {
    val ids = request.bodyAs[js.Array[String]]
    userDAO.findByIDs(ids) onComplete {
      case Success(users) => response.send(users); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
