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
  app.get("/api/user/:userID/favorites", (request: Request, response: Response, next: NextFunction) => listFavoriteSymbols(request, response, next))
  app.get("/api/user/:userID/recents", (request: Request, response: Response, next: NextFunction) => listRecentSymbols(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /*
  Seq(
    ("47d09c0a-55d2-11ea-a02d-0800273905de", "ldaniels", "./public/images/avatars/gears.jpg"),
    ("47d1d41d-55d2-11ea-a02d-0800273905de", "gunst4rhero", "./public/images/avatars/gunstar-heroes.jpg"),
    ("47d27ded-55d2-11ea-a02d-0800273905de", "gadget", "./public/images/avatars/dcu.png"),
    ("47d2e18c-55d2-11ea-a02d-0800273905de", "daisy", "./public/images/avatars/daisy.jpg"),
    ("8c9c9d63-5662-11ea-a02d-0800273905de", "ldaniels528", "./public/images/avatars/fugitive528.jpg")) foreach { case (uid, name, path) =>
    writeImage(userID = uid, name = name, path = path) onComplete {
      case Success(value) => println(s"$name ~> $path: count = $value")
      case Failure(e) => e.printStackTrace()
    }
  }*/

  def addFavoriteSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val (userID, symbol) = (request.params("userID"), request.params("symbol"))
    userDAO.addFavoriteSymbol(userID, symbol) onComplete {
      case Success(result)  => response.send(new Ok(result)); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def listFavoriteSymbols(request: Request, response: Response, next: NextFunction): Unit = {
    val userID= request.params("userID")
    userDAO.findFavoriteSymbols(userID) onComplete {
      case Success(results)  => response.send(results); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def addRecentSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val (userID, symbol) = (request.params("userID"), request.params("symbol"))
    userDAO.addRecentSymbol(userID, symbol) onComplete {
      case Success(result) if result > 0 =>
        response.send(new Ok(result)); next()
      case Success(_) =>
        response.badRequest(request.params); next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  def listRecentSymbols(request: Request, response: Response, next: NextFunction): Unit = {
    val userID= request.params("userID")
    userDAO.findRecentSymbols(userID) onComplete {
      case Success(results)  => response.send(results); next()
      case Failure(e) => response.internalServerError(e); next()
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

  def writeImage(userID: String, name: String, path: String): Future[Int] = {
    val suffix = path.lastIndexOf(".") match {
      case -1 => None
      case index => Some(path.substring(index + 1).toLowerCase())
    }
    val mime = suffix match {
      case None => "image/png"
      case Some("jpg") => "image/jpeg"
      case Some(imageType) => s"image/$imageType"
    }
    for {
      data <- Fs.readFileFuture(path)
      w <- userDAO.createIcon(new UserIconData(userID = userID, name = name, mime = mime, image = data))
    } yield w
  }

}
