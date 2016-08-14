package com.shocktrade.javascript.routes

import com.shocktrade.javascript.data.ProfileDAO
import com.shocktrade.javascript.data.ProfileDAO._
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Profile Routes
  * @author lawrence.daniels@gmail.com
  */
object ProfileRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val profileDAO = dbFuture.flatMap(_.getProfileDAO)

    app.get("/api/profile/facebook/:id", (request: Request, response: Response, next: NextFunction) => getProfileByFBID(request, response, next))
  }

  def getProfileByFBID(request: Request, response: Response, next: NextFunction)(implicit profileDAO: Future[ProfileDAO]) = {
    val fbId = request.params("id")
    profileDAO.flatMap(_.findByFacebookID(fbId)) onComplete {
      case Success(Some(profile)) => response.send(profile); next()
      case Success(None) => response.notFound(fbId); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
