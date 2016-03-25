package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper.{BigDecimalHandler => BDH}
import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents a Margin Account
  * @author lawrence.daniels@gmail.com
  */
case class MarginAccount(cashFunds: BigDecimal = 0.00,
                         borrowedFunds: BigDecimal = 0.00,
                         interestPaid: BigDecimal = 0.00,
                         interestPaidAsOfDate: Date = new Date(),
                         asOfDate: Date = new Date())

/**
  * Margin Account Singleton
  * @author lawrence.daniels@gmail.com
  */
object MarginAccount {
  val InitialMargin: BigDecimal = 0.50
  val InterestRate: BigDecimal = 0.015
  val MaintenanceMargin: BigDecimal = 0.25

  implicit val MarginAccountFormat = Json.format[MarginAccount]

  implicit val MarginAccountHandler = Macros.handler[MarginAccount]

}