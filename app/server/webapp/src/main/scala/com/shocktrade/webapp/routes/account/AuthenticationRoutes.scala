package com.shocktrade.webapp.routes.account

import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Random, Success}

/**
 * Authentication Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AuthenticationRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val authenticationDAO = AuthenticationDAO()

  // authentication API
  app.get("/api/auth/code", (request: Request, response: Response, next: NextFunction) => code(request, response, next))
  app.post("/api/auth/login", (request: Request, response: Response, next: NextFunction) => login(request, response, next))
  app.delete("/api/auth/logout", (request: Request, response: Response, next: NextFunction) => logout(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def code(request: Request, response: Response, next: NextFunction): Unit = {
    val charset = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Seq('_', '-')
    val random = new Random()
    val code = new String((for (_ <- 1 to 16; c = charset(random.nextInt(charset.length))) yield c).toArray)
    response.send(new AuthenticationCode(code))
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
          case Success(Some(credential)) =>
            response.send(new AuthenticationResponse(userID = credential.userID)); next()
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
    response.internalServerError("Not yet implemented")
  }

}
