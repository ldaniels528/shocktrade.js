package com.shocktrade.common.models.user

import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an application user
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
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

  def apply(_id: js.UndefOr[String], facebookID: js.UndefOr[String], name: js.UndefOr[String]) = {
    val user = New[User]
    user._id = _id
    user.facebookID = facebookID
    user.name = name
    user
  }

  /**
    * User Enrichment
    * @param user the given [[User user]]
    */
  implicit class UserEnrichment(val user: User) extends AnyVal {

    @inline
    def is(userId: js.UndefOr[String]) = user._id ?== userId

  }

}