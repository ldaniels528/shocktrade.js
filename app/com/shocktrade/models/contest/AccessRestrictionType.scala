package com.shocktrade.models.contest

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * Represents an enumeration of Contest Access Restriction
 * @author lawrence.daniels@gmail.com
 */
object AccessRestrictionType extends Enumeration {
  type AccessRestrictionType = Value

  val FRIENDS_ONLY = Value("Friends-Only")
  val INVITATION_ONLY = Value("Invitation-Only")

  /**
   * Access Restriction Type JSON Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object AccessRestrictionTypeFormat extends Format[AccessRestrictionType] {

    def reads(json: JsValue) = JsSuccess(AccessRestrictionType.withName(json.as[String]))

    def writes(priceType: AccessRestrictionType) = JsString(priceType.toString)
  }

  /**
   * Access Restriction Type BSON Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object AccessRestrictionTypeHandler extends BSONHandler[BSONString, AccessRestrictionType] {

    def read(string: BSONString) = AccessRestrictionType.withName(string.value)

    def write(priceType: AccessRestrictionType) = BSONString(priceType.toString)
  }

}
