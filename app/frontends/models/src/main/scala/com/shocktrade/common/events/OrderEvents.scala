package com.shocktrade.common.events

import com.shocktrade.common.events.RemoteEvent._

/**
 * Represents an order creation event
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderEvents {

  def updated(portfolioId: String): RemoteEvent = {
    new RemoteEvent(action = OrderUpdated, data = portfolioId)
  }

}
