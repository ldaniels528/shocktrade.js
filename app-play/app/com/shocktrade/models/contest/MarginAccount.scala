package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

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

  implicit val marginAccountReads = Json.reads[MarginAccount]
  implicit val marginAccountWrites = Json.writes[MarginAccount]

  implicit object MarginAccountReader extends BSONDocumentReader[MarginAccount] {
    def read(doc: BSONDocument) = MarginAccount(
      doc.getAs[BigDecimal]("cashFunds").get,
      doc.getAs[BigDecimal]("borrowedFunds").get,
      doc.getAs[BigDecimal]("interestPaid").get,
      doc.getAs[Date]("interestPaidAsOfDate").get,
      doc.getAs[Date]("asOfDate").get
    )
  }

  implicit object MarginAccountWriter extends BSONDocumentWriter[MarginAccount] {
    def write(marginAccount: MarginAccount) = BSONDocument(
      "cashFunds" -> marginAccount.cashFunds,
      "borrowedFunds" -> marginAccount.borrowedFunds,
      "interestPaid" -> marginAccount.interestPaid,
      "interestPaidAsOfDate" -> marginAccount.interestPaidAsOfDate,
      "asOfDate" -> marginAccount.asOfDate
    )
  }

}