package com.shocktrade.remote.proxies

import com.shocktrade.common.Ok
import com.shocktrade.common.api.RemoteEventAPI
import com.shocktrade.common.events.RemoteEvent

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Remote Event Proxy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RemoteEventProxy(host: String, port: Int) extends Proxy with RemoteEventAPI {
  override val baseURL = s"http://$host:$port"

  def relayEvent(event: RemoteEvent): Future[Ok] = post(relayEventURL, event)

}