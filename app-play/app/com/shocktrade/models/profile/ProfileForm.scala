package com.shocktrade.models.profile

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Profile Form
  * @param userName
  * @param facebookID
  * @param email
  */
case class ProfileForm(userName: String, facebookID: String, email: Option[String])

/**
  * Profile Form Companion Object
  */
object ProfileForm {

  implicit val profileFormReads: Reads[ProfileForm] = (
    (__ \ "userName").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "email").readNullable[String]) (ProfileForm.apply _)

}
