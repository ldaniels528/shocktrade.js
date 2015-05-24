package com.shocktrade.models.contest

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * Represents an enumeration of account types
 * @author lawrence.daniels@gmail.com
 */
object AccountTypes extends Enumeration {
  type AccountType = Value

  val CASH = Value("CASH")
  val MARGIN = Value("MARGIN")

  /**
   * Order Type Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object AccountTypeFormat extends Format[AccountType] {

    def reads(json: JsValue) = JsSuccess(AccountTypes.withName(json.as[String]))

    def writes(accountType: AccountType) = JsString(accountType.toString)
  }

  /**
   * Order Type Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object AccountTypeHandler extends BSONHandler[BSONString, AccountType] {

    def read(string: BSONString) = AccountTypes.withName(string.value)

    def write(accountType: AccountType) = BSONString(accountType.toString)
  }


}
