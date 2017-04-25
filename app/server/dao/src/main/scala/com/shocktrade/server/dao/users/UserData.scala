package com.shocktrade.server.dao.users

import com.shocktrade.common.models.user.UserLike
import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a User data model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait UserData extends UserLike {
  var _id: js.UndefOr[ObjectID]

}
