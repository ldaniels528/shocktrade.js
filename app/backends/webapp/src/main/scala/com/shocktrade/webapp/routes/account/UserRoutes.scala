package com.shocktrade.webapp.routes.account

import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.webapp.routes.{NextFunction, Ok}
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
  private val userDAO = UserDAO()

  // individual item
  app.post("/api/user", (request: Request, response: Response, next: NextFunction) => createAccount(request, response, next))
  app.get("/api/user/:userID", (request: Request, response: Response, next: NextFunction) => userByID(request, response, next))
  app.get("/api/user/:userID/icon", (request: Request, response: Response, next: NextFunction) => userIcon(request, response, next))
  app.get("/api/user/:userID/netWorth", (request: Request, response: Response, next: NextFunction) => computeNetWorth(request, response, next))
  app.put("/api/user/:userID/favorite/:symbol", (request: Request, response: Response, next: NextFunction) => addFavoriteSymbol(request, response, next))
  app.put("/api/user/:userID/recent/:symbol", (request: Request, response: Response, next: NextFunction) => addRecentSymbol(request, response, next))

  // collections
  app.put("/api/users", (request: Request, response: Response, next: NextFunction) => usersByID(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /*
    writeImage(userID = "47d09c0a-55d2-11ea-a02d-0800273905de", name = "dcu", mime = "image/png", path = "./public/images/avatars/dcu.png") onComplete {
      case Success(value) => println(s"w = $value")
      case Failure(e) =>
        e.printStackTrace()
    }*/

  def addFavoriteSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val (userID, symbol) = (request.params("userID"), request.params("symbol"))
    userDAO.addFavoriteSymbol(userID, symbol) onComplete {
      case Success(result) if result == 1 =>
        response.send(new Ok(result)); next()
      case Success(_) =>
        response.badRequest(request.params); next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  def addRecentSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val (userID, symbol) = (request.params("userID"), request.params("symbol"))
    userDAO.addRecentSymbol(userID, symbol) onComplete {
      case Success(result) if result == 1 =>
        response.send(new Ok(result)); next()
      case Success(_) =>
        response.badRequest(request.params); next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  def createAccount(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[SignUpForm]
    val args = (for {
      username <- form.username
      email <- form.email
      password <- form.password
      passwordConfirm <- form.passwordConfirm
    } yield (username, email, password, passwordConfirm)).toOption

    args match {
      case Some((username, email, password, passwordConfirm)) =>
        val outcome = for {
          account_? <- userDAO.createAccount(new UserAccountData(
            username = username,
            email = email,
            password = password,
            wallet = 250e+3,
          ))
        } yield account_?

        outcome onComplete {
          case Success(Some(accountData)) =>
            response.send(accountData.copy(password = js.undefined)); next()
          case Success(None) =>
            response.internalServerError("User account could not be created")
          case Failure(e) =>
            response.internalServerError(e.getMessage); next()
        }
      case None =>
        response.badRequest("The username and password are required"); next()
    }
  }

  def computeNetWorth(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    userDAO.computeNetWorth(userID) onComplete {
      case Success(Some(netWorth)) => response.send(netWorth)
      case Success(None) => response.notFound(request.params)
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  def userByID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    userDAO.findByID(userID) onComplete {
      case Success(Some(user)) => response.send(user); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def usersByID(request: Request, response: Response, next: NextFunction): Unit = {
    val userIDs = request.bodyAs[js.Array[String]]
    userDAO.findByIDs(userIDs) onComplete {
      case Success(users) => response.send(users); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def userIcon(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val outcome = for {
      icon_? <- userDAO.findIcon(userID)
      (mime, image) <- icon_? match {
        case Some(icon) => Future.successful((icon.mime, icon.image))
        case None => Fs.readFileFuture("./public/images/avatars/avatar100.png").map(image => ("image/png": js.UndefOr[String], image))
      }
    } yield (mime, image)

    outcome onComplete {
      case Success((mime, image)) =>
        mime.foreach(response.setContentType)
        response.send(image)
        next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  private def writeImage(userID: String, name: String, mime: String, path: String): Future[Int] = {
    for {
      data <- Fs.readFileFuture(path)
      w <- userDAO.createIcon(new UserIconData(userID = userID, name = name, mime = mime, image = data))
    } yield w
  }

}
