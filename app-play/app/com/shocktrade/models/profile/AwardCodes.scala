package com.shocktrade.models.profile

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
  * Contest Award Codes
  * @author lawrence.daniels@gmail.com
  */
object AwardCodes extends Enumeration {
  type AwardCode = Value

  val FACEBOOK, FBLIKEUS, TWITTER, LINKEDIN, GOOGPLUS, INSTGRAM, MEPROMO,
  SOCLITE, PERKS, PERKSET, EUROTACT, INTNSHPR, PAYDIRT, MADMONEY, CRYSTBAL,
  CHKDFLAG, GLDTRPHY = Value

  /**
    * Award Code Format
    * @author lawrence.daniels@gmail.com
    */
  implicit object AwardCodeFormat extends Format[AwardCode] {

    def reads(json: JsValue) = JsSuccess(AwardCodes.withName(json.as[String]))

    def writes(orderTerm: AwardCode) = JsString(orderTerm.toString)
  }

  /**
    * Award Code Handler
    * @author lawrence.daniels@gmail.com
    */
  implicit object AwardCodeHandler extends BSONHandler[BSONString, AwardCode] {

    def read(string: BSONString) = AwardCodes.withName(string.value)

    def write(orderTerm: AwardCode) = BSONString(orderTerm.toString)
  }

}
