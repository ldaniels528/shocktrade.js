package com.shocktrade.server.dao.events

import scala.scalajs.js

/**
  * Represents an Event to be Sourced
  * @author lawrence.daniels@gmail.com
  */
trait SourcedEvent extends js.Object {

  def name: js.UndefOr[String]

  def uuid: js.UndefOr[String]

  def userID: js.UndefOr[String]

  def creationTime: js.UndefOr[js.Date]

}