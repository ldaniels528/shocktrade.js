package com.shocktrade.webapp.routes.account

import com.shocktrade.common.auth.AuthenticationResponse
import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * User Account Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserAccountRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val userAccountDAO = UserAccountDAO()

  // account management API
  app.post("/api/account", (request: Request, response: Response, next: NextFunction) => createAccount(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

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
          userID_? <- userAccountDAO.createAccount(new UserAccountData(
            username = username,
            email = email,
            password = password,
            wallet = 250e+3,
          ))
        } yield userID_?

        outcome onComplete {
          case Success(Some(userID)) =>
            response.send(new AuthenticationResponse(userID = userID)); next()
          case Success(None) =>
            response.internalServerError("User account could not be created")
          case Failure(e) =>
            response.internalServerError(e.getMessage); next()
        }
      case None =>
        response.badRequest("The username and password are required"); next()
    }
  }

}
