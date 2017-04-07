package com.shocktrade.server.services

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.http.IncomingMessage
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.request.{Request, RequestOptions}

import scala.concurrent.Promise

/**
  * Remote Event Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RemoteEventService(webAppEndPoint: String) {
  private val logger = LoggerFactory.getLogger(getClass)

  def send(event: RemoteEvent): Promise[(IncomingMessage, String)] = {
    logger.log("Transmitting %j to '%s'...", Moment().format("MM/DD HH:mm:ss"), event, webAppEndPoint)
    Request.postAsync(new RequestOptions(url = s"http://$webAppEndPoint/api/events/relay", form = event))
  }

}