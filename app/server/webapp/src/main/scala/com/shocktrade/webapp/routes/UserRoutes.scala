package com.shocktrade.webapp.routes

import com.shocktrade.common.forms.FacebookFriendForm
import com.shocktrade.common.models.user.FriendStatus
import com.shocktrade.server.dao.users.UserDAO._

import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * User Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object UserRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) = {
    implicit val userDAO = dbFuture.flatMap(_.getUserDAO)

    app.post("/api/friend/status", (request: Request, response: Response, next: NextFunction) => friendStatus(request, response, next))
    app.get("/api/user/:id", (request: Request, response: Response, next: NextFunction) => userByID(request, response, next))
    app.put("/api/users", (request: Request, response: Response, next: NextFunction) => usersByID(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
      * Retrieves the status for a user's friend
      */
    def friendStatus(request: Request, response: Response, next: NextFunction) = {
      val form = request.bodyAs[FacebookFriendForm].values
      form match {
        case Some((fbId, name)) =>
          userDAO.flatMap(_.findFriendByFacebookID(fbId)) onComplete {
            case Success(Some(status)) => response.send(status); next()
            case Success(None) => response.send(new FriendStatus(facebookID = fbId, name = name, status = "Non-member")); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None =>
          response.badRequest("One or more require fields (id, name) is missing")
      }
    }

    /**
      * Retrieves a user by ID
      */
    def userByID(request: Request, response: Response, next: NextFunction) = {
      val id = request.params("id")
      userDAO.flatMap(_.findUserByID(id)) onComplete {
        case Success(Some(user)) => response.send(user); next()
        case Success(None) => response.notFound(id); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves an array of users by ID
      */
    def usersByID(request: Request, response: Response, next: NextFunction) = {
      val ids = request.bodyAs[js.Array[String]]
      userDAO.flatMap(_.findUsersByID(ids)) onComplete {
        case Success(users) => response.send(users); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
