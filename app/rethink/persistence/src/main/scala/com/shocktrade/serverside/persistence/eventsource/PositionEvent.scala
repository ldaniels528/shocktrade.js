package com.shocktrade.serverside.persistence.eventsource

import scala.scalajs.js

/**
  * Represents a generic Position Event
  * @author lawrence.daniels@gmail.com
  */
trait PositionEvent extends OrderEvent {

  def positionID: js.UndefOr[String]

}
