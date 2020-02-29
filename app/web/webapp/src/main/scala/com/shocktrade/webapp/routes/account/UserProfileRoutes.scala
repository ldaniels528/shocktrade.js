package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.NetWorth
import com.shocktrade.webapp.routes.{NextFunction, Ok}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * User Profile Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfileRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val profileDAO = UserAccountDAO()
  private val userAccountDAO = UserAccountDAO()

  app.get("/api/profile/:userID/netWorth", (request: Request, response: Response, next: NextFunction) => netWorth(request, response, next))
  app.put("/api/profile/:userID/recent/:symbol", (request: Request, response: Response, next: NextFunction) => addRecentSymbol(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def addRecentSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val symbol = request.params("symbol")
    profileDAO.addRecentSymbol(userID, symbol) onComplete {
      case Success(result) if result == 1 =>
        response.send(new Ok(result)); next()
      case Success(result) =>
        response.badRequest(result); next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  def netWorth(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val outcome = for {
      user <- userAccountDAO.computeNetWorth(userID)
    } yield user

    outcome onComplete {
      case Success(total) => response.send(new NetWorth(total.orUndefined))
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

}
