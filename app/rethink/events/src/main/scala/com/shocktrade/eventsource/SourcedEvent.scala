package com.shocktrade.eventsource

import scala.scalajs.js

/**
  * Represents an Event to be Sourced
  * @author lawrence.daniels@gmail.com
  */
trait SourcedEvent extends js.Object {

  def name: js.UndefOr[String]

  def uuid: js.UndefOr[String]

}
