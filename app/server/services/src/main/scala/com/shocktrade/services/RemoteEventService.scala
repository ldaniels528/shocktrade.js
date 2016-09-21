package com.shocktrade.services

import com.shocktrade.common.events.RemoteEvent
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.request.{Request, RequestOptions}
import org.scalajs.nodejs.{NodeRequire, console}

/**
  * Remote Event Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RemoteEventService(webAppEndPoint: String)(implicit require: NodeRequire) {
  private implicit val moment = Moment()
  private val request = Request()

  def send(event: RemoteEvent) = {
    console.log("%s [%s] Transmitting %j to '%s'...", moment().format("MM/DD HH:mm:ss"), getClass.getName, event, webAppEndPoint)
    request.postFuture(new RequestOptions(url = s"http://$webAppEndPoint/api/events/relay", form = event))
  }

}