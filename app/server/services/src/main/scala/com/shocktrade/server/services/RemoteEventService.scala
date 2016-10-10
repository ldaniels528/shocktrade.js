package com.shocktrade.server.services

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.server.common.LoggerFactory
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.request.{Request, RequestOptions}

/**
  * Remote Event Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RemoteEventService(webAppEndPoint: String)(implicit require: NodeRequire) {
  private val logger = LoggerFactory.getLogger(getClass)
  private implicit val moment = Moment()
  private val request = Request()

  def send(event: RemoteEvent) = {
    logger.log("Transmitting %j to '%s'...", moment().format("MM/DD HH:mm:ss"), event, webAppEndPoint)
    request.postFuture(new RequestOptions(url = s"http://$webAppEndPoint/api/events/relay", form = event))
  }

}