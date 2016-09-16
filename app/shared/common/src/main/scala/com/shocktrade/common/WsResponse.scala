package com.shocktrade.common

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a web-socket object response
  * @param action the given action
  * @param data   the given data payload
  */
@ScalaJSDefined
class WsResponse(val action: js.UndefOr[String], val data: js.UndefOr[String]) extends js.Object
