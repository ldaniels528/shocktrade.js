package com.shocktrade.webapp.routes

import com.shocktrade.common.Ok
import com.shocktrade.common.api.RemoteEventAPI
import com.shocktrade.common.events.RemoteEvent
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext

/**
 * Remote Event Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RemoteEventRoutes(app: Application)(implicit ec: ExecutionContext) extends RemoteEventAPI with RemoteEventSupport {

  // define the API
  app.post(relayEventURL, (request: Request, response: Response, next: NextFunction) => relayEvent(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def relayEvent(request: Request, response: Response, next: NextFunction): Unit = {
    val event = request.bodyAs[RemoteEvent]
    try {
      wsEmit(event)
      response.send(Ok(1))
      next()
    } catch {
      case e: Exception => response.showException(e).internalServerError(e); next()
    }
  }

}
