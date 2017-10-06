package com.shocktrade.serverside.persistence.eventsource

import com.shocktrade.eventsource.SourcedEvent

import scala.scalajs.js

/**
  * Represents a generic Order Event
  * @author lawrence.daniels@gmail.com
  */
trait OrderEvent extends SourcedEvent {

  def orderID: js.UndefOr[String]

  def effectiveTime: js.UndefOr[js.Date]

}
