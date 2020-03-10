package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.UserLike

import scala.scalajs.js

/**
 * Represents a User data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserData extends UserLike {
  var userID: js.UndefOr[String]

}
