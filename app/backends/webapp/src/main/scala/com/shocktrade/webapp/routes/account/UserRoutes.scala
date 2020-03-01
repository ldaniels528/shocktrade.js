package com.shocktrade.webapp.routes.account

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * User Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val userDAO = UserDAO()

  // individual item
  app.get("/api/user/:id", (request: Request, response: Response, next: NextFunction) => userByID(request, response, next))
  app.get("/api/user/icon/:iconID", (request: Request, response: Response, next: NextFunction) => getIcon(request, response, next))

  // collections
  app.put("/api/users", (request: Request, response: Response, next: NextFunction) => usersByID(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

/*
  writeImage(userID = "8c9c9d63-5662-11ea-a02d-0800273905de", name = "fugitive528", mime = "image/jpeg", path = "./public/images/avatars/fugitive528.jpeg") onComplete {
    case Success(value) => logger.info(s"w = $value")
    case Failure(e) =>
      e.printStackTrace()
  }*/

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

  def getIcon(request: Request, response: Response, next: NextFunction): Unit = {
    val iconID = request.params("iconID")
    userDAO.findIcon(iconID) onComplete {
      case Success(Some(icon)) =>
        icon.mime.foreach(response.setContentType)
        response.send(icon.image)
        next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  private def writeImage(userID: String, name: String, mime: String, path: String): Future[Int] = {
    for {
      data <- Fs.readFileFuture(path)
      w <- userDAO.createIcon(userID = userID, name = name, mime = mime, data)
    } yield w
  }

}
