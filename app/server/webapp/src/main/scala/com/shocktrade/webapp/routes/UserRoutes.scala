package com.shocktrade.webapp.routes

import com.shocktrade.server.dao.users.UserDAO._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * User Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object UserRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val userDAO = dbFuture.flatMap(_.getUserDAO)

    app.get("/api/user/:id", (request: Request, response: Response, next: NextFunction) => userByID(request, response, next))
    app.put("/api/users", (request: Request, response: Response, next: NextFunction) => usersByID(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def userByID(request: Request, response: Response, next: NextFunction) = {
      val id = request.params("id")
      userDAO.flatMap(_.findUserByID(id)) onComplete {
        case Success(Some(user)) => response.send(user); next()
        case Success(None) => response.notFound(id); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def usersByID(request: Request, response: Response, next: NextFunction) = {
      val ids = request.bodyAs[js.Array[String]]
      userDAO.flatMap(_.findUsersByID(ids)) onComplete {
        case Success(users) => response.send(users); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
