package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a margin account
 * @author lawrence.daniels@gmail.com
 */
case class MarginAccount(cashFunds: BigDecimal = 0.00,
                         borrowedFunds: BigDecimal = 0.00,
                         interestPaid: BigDecimal = 0.00,
                         asOfDate: Date = new Date())

/**
 * Margin Account Singleton
 * @author lawrence.daniels@gmail.com
 */
object MarginAccount {
  val InitialMargin: BigDecimal = 0.50
  val MaintenanceMargin: BigDecimal = 0.25
  val InterestRate: BigDecimal = 0.015

  implicit val marginAccountReads: Reads[MarginAccount] = (
    (__ \ "cashFunds").read[BigDecimal] and
      (__ \ "borrowedFunds").read[BigDecimal] and
      (__ \ "interestPaid").read[BigDecimal] and
      (__ \ "asOfDate").read[Date])(MarginAccount.apply _)

  implicit val marginAccountWrites: Writes[MarginAccount] = (
    (__ \ "cashFunds").write[BigDecimal] and
      (__ \ "borrowedFunds").write[BigDecimal] and
      (__ \ "interestPaid").write[BigDecimal] and
      (__ \ "asOfDate").write[Date])(unlift(MarginAccount.unapply))

  implicit object MarginAccountReader extends BSONDocumentReader[MarginAccount] {
    def read(doc: BSONDocument) = MarginAccount(
      doc.getAs[BigDecimal]("cashFunds").get,
      doc.getAs[BigDecimal]("borrowedFunds").get,
      doc.getAs[BigDecimal]("interestPaid").getOrElse(0.00),
      doc.getAs[Date]("asOfDate").get
    )
  }

  implicit object MarginAccountWriter extends BSONDocumentWriter[MarginAccount] {
    def write(marginAccount: MarginAccount) = BSONDocument(
      "cashFunds" -> marginAccount.cashFunds,
      "borrowedFunds" -> marginAccount.borrowedFunds,
      "interestPaid" -> marginAccount.interestPaid,
      "asOfDate" -> marginAccount.asOfDate
    )
  }

}