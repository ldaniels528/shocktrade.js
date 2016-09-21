package com.shocktrade.webapp.routes

import com.shocktrade.common.events.RemoteEvent
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}

/**
  * Remote Event Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RemoteEventRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) = {

    // define the API
    app.post("/api/events/relay", (request: Request, response: Response, next: NextFunction) => relayEvent(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def relayEvent(request: Request, response: Response, next: NextFunction) = {
      val form = request.bodyAs[RemoteEvent]
      val result = for {
        action <- form.action
        data <- form.data
      } yield (action, data)

      result.toOption match {
        case Some((action, data)) =>
          WebSocketHandler.emit(action, data)
          response.send("Ok")
          next()
        case None =>
          console.error("invalid event => %j", form)
          response.badRequest(form)
          next()
      }
    }
  }

}
