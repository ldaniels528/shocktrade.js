package com.shocktrade.common.models.user

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a user-like model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait UserLike extends js.Object {
  var facebookID: js.UndefOr[String]
  var name: js.UndefOr[String]

}
