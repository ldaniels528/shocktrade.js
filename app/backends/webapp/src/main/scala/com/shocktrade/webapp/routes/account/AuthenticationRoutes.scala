package com.shocktrade.webapp.routes.account

import com.shocktrade.common.Ok
import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import com.shocktrade.common.models.user.OnlineStatus
import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.account.dao.AuthenticationDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Random, Success}

/**
 * Authentication Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AuthenticationRoutes(app: Application)(implicit ec: ExecutionContext, authenticationDAO: AuthenticationDAO) {
  private val onlineStatuses = js.Dictionary[OnlineStatus]()

  // authentication API
  app.get("/api/auth/code", (request: Request, response: Response, next: NextFunction) => code(request, response, next))
  app.post("/api/auth/login", (request: Request, response: Response, next: NextFunction) => login(request, response, next))
  app.post("/api/auth/logout", (request: Request, response: Response, next: NextFunction) => logout(request, response, next))

  // session API
  app.get("/api/online", (request: Request, response: Response, next: NextFunction) => statusAll(request, response, next))
  app.get("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => statusByUserID(request, response, next))
  app.put("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => onlineByUserID(request, response, next))
  app.delete("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => offlineByUserID(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      Authentication API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def code(request: Request, response: Response, next: NextFunction): Unit = {
    val charset = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Seq('_', '-')
    val random = new Random()
    val code = new String((for (_ <- 1 to 16; c = charset(random.nextInt(charset.length))) yield c).toArray)
    response.send(new AuthenticationCode(code))
    next()
  }

  def login(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[AuthenticationForm]
    val args = (for {
      username <- form.username
      password <- form.password
      authCode <- form.authCode
    } yield (username, password, authCode)).toOption

    args match {
      case Some((username, password, authCode)) =>
        authenticationDAO.findByUsername(username) onComplete {
          case Success(Some(accountData)) =>
            response.send(new AuthenticationResponse(
              userID = accountData.userID,
              username = accountData.username,
              email = accountData.email,
              wallet = accountData.wallet
            ));
            next()
          case Success(None) =>
            response.notFound(form); next()
          case Failure(e) =>
            response.internalServerError(e.getMessage); next()
        }
      case None =>
        response.badRequest("The username and password are required"); next()
    }
  }

  def logout(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(Ok(updateCount = 1)); next()
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Online Status API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def statusAll(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(onlineStatuses)
    next()
  }

  def statusByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
    response.send(status)
    next()
  }

  def onlineByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(connected = true))
    status.connected = true
    response.send(status)
    next()
  }

  def offlineByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
    status.connected = false
    response.send(status)
    next()
  }

}
