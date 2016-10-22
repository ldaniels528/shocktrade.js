package com.shocktrade.common.models.user

import org.scalajs.sjs.JsUnderOrHelper._

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an application user
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class User(var _id: js.UndefOr[String],
           var facebookID: js.UndefOr[String],
           var name: js.UndefOr[String]) extends UserLike

/**
  * User Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object User {

  implicit class Profile2User(val profile: ProfileLike) extends AnyVal {

    @inline
    def toUser: User = profile.asInstanceOf[User]

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