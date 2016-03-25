package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper.{BigDecimalHandler => BDH}
import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents a cash account
  * @author lawrence.daniels@gmail.com
  */
case class CashAccount(cashFunds: BigDecimal = 0.00, asOfDate: Date = new Date())

/**
  * Cash Account Singleton
  * @author lawrence.daniels@gmail.com
  */
object CashAccount {

  implicit val CashAccountFormat = Json.format[CashAccount]

  implicit val CashAccountHandler = Macros.handler[CashAccount]

}
