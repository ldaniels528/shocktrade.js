package com.shocktrade.webapp.routes

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext

/**
 * Remote Event Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RemoteEventRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  // define the API
  app.post("/api/events/relay", (request: Request, response: Response, next: NextFunction) => relayEvent(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def relayEvent(request: Request, response: Response, next: NextFunction): Unit = {
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
        logger.error("BadRequest: invalid event => %j", form)
        response.badRequest(form)
        next()
    }
  }

}
