package com.shocktrade.webapp.routes.social

import com.shocktrade.common.models.user.OnlineStatus
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
 * Online Status Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OnlineStatusRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val statuses = js.Dictionary[OnlineStatus]()

  app.get("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => statusByUserID(request, response, next))
  app.put("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => onlineByUserID(request, response, next))
  app.delete("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => offlineByUserID(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def statusByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
    response.send(status)
    next()
  }

  def onlineByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = true))
    status.connected = true
    response.send(status)
    next()
  }

  def offlineByUserID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = statuses.getOrElseUpdate(userID, new OnlineStatus(connected = false))
    status.connected = false
    response.send(status)
    next()
  }

}
