package com.shocktrade.webapp.routes

import org.scalajs.nodejs.util.ScalaJsHelper._
import com.shocktrade.common.dao.contest.ProfileDAO._
import com.shocktrade.common.dao.contest.ProfileData
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Profile Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ProfileRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val profileDAO = dbFuture.flatMap(_.getProfileDAO)

    app.get("/api/profile/facebook/:fbID", (request: Request, response: Response, next: NextFunction) => profileByFBID(request, response, next))
    app.put("/api/profile/:userID/recent/:symbol", (request: Request, response: Response, next: NextFunction) => addRecentSymbol(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def addRecentSymbol(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params("userID")
      val symbol = request.params("symbol")
      profileDAO.flatMap(_.addRecentSymbol(userID, symbol)) onComplete {
        case Success(result) if result.isOk =>
          result.valueAs[ProfileData] match {
            case Some(profile) => response.send(profile); next()
            case None => response.notFound(userID)
          }
        case Success(result) => response.badRequest(result)
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def profileByFBID(request: Request, response: Response, next: NextFunction) = {
      val fbId = request.params("fbID")
      profileDAO.flatMap(_.findOneByFacebookID(fbId)) onComplete {
        case Success(Some(profile)) => response.send(profile); next()
        case Success(None) => response.notFound(fbId); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
