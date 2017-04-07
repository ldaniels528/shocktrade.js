package com.shocktrade.webapp.routes

import com.shocktrade.common.models.user.OnlineStatus
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Online Status Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object OnlineStatusRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) = {
    val statuses = js.Dictionary[OnlineStatus]()

    app.get("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => statusByUserID(request, response, next))
    app.put("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => onlineByUserID(request, response, next))
    app.delete("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => offlineByUserID(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def statusByUserID(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params.apply("userID")
      val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
      response.send(status)
      next()
    }

    def onlineByUserID(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params.apply("userID")
      val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = true))
      status.connected = true
      response.send(status)
      next()
    }

    def offlineByUserID(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params.apply("userID")
      val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
      status.connected = false
      response.send(status)
      next()
    }

  }

}
