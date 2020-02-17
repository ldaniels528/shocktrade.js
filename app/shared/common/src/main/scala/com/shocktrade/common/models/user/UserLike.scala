package com.shocktrade.common.models.user

import scala.scalajs.js

/**
  * Represents a user-like model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait UserLike extends js.Object {
  var facebookID: js.UndefOr[String]
  var name: js.UndefOr[String]
  var wallet: js.UndefOr[Double]

}
