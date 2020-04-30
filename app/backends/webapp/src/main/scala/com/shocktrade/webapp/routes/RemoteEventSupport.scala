package com.shocktrade.webapp.routes

import com.shocktrade.common.events.RemoteEvent
import io.scalajs.nodejs.timers.Immediate

/**
 * Remote Event Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RemoteEventSupport {

  def wsEmit(event: RemoteEvent): Immediate = WebSocketHandler.emit(event)

}
