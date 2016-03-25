package com.shocktrade.models.profile

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Profile Form
  * @param userName   the given user name
  * @param facebookID the given Facebook ID
  * @param email      the given email address
  * @author lawrence.daniels@gmail.com
  */
case class ProfileForm(userName: String, facebookID: String, email: Option[String])

/**
  * Profile Form Companion Object
  * @author lawrence.daniels@gmail.com
  */
object ProfileForm {

  implicit val ProfileFormFormat = Json.format[ProfileForm]

  implicit val ProfileFormHandler = Macros.handler[ProfileForm]

}
