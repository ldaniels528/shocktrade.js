package com.shocktrade.server.dao.users

import com.shocktrade.common.models.user.UserLike
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a User data model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait UserData extends UserLike {
  var _id: js.UndefOr[ObjectID]

}
