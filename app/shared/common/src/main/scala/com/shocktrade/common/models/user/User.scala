package com.shocktrade.common.models.user

import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Represents an application user
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait User extends UserLike {
  var _id: js.UndefOr[String]
  var facebookID: js.UndefOr[String]
  var name: js.UndefOr[String]
  var description: js.UndefOr[String]
}

/**
  * User Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object User {

  def apply(_id: js.UndefOr[String],
            facebookID: js.UndefOr[String],
            name: js.UndefOr[String],
            description: js.UndefOr[String] = js.undefined): User = {
    val user = New[User]
    user._id = _id
    user.facebookID = facebookID
    user.name = name
    user.description = description
    user
  }

  /**
    * User Enrichment
    * @param user the given [[User user]]
    */
  implicit class UserEnrichment(val user: User) extends AnyVal {

    @inline
    def is(userId: js.UndefOr[String]): Boolean = user._id ?== userId

  }

}