package com.shocktrade.javascript.routes

import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Online Status Routes
  * @author lawrence.daniels@gmail.com
  */
object OnlineStatusRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {

    app.put("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => online(request, response, next))
    app.delete("/api/online/:userID", (request: Request, response: Response, next: NextFunction) => offline(request, response, next))
  }

  def online(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB) = {
    response.status(200)
  }

  def offline(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB) = {
    response.status(200)
  }

}
