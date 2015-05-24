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
case class MarginAccount(depositedFunds: Double,
                         cashInvestedAmount: Double = 0.00,
                         asOfDate: Date = new Date(),
                         initialMargin: Double = 0.50,
                         maintenanceMargin: Double = 0.30,
                         interestRate: Double = 0.10)

/**
 * Margin Account Singleton
 * @author lawrence.daniels@gmail.com
 */
object MarginAccount {

  implicit val marginAccountReads: Reads[MarginAccount] = (
    (__ \ "depositedFunds").read[Double] and
      (__ \ "cashInvestedAmount").read[Double] and
      (__ \ "asOfDate").read[Date] and
      (__ \ "initialMargin").read[Double] and
      (__ \ "maintenanceMargin").read[Double] and
      (__ \ "interestRate").read[Double])(MarginAccount.apply _)

  implicit val marginAccountWrites: Writes[MarginAccount] = (
    (__ \ "depositedFunds").write[Double] and
      (__ \ "cashInvestedAmount").write[Double] and
      (__ \ "asOfDate").write[Date] and
      (__ \ "initialMargin").write[Double] and
      (__ \ "maintenanceMargin").write[Double] and
      (__ \ "interestRate").write[Double])(unlift(MarginAccount.unapply))

  implicit object MarginAccountReader extends BSONDocumentReader[MarginAccount] {
    def read(doc: BSONDocument) = MarginAccount(
      doc.getAs[Double]("depositedFunds").get,
      doc.getAs[Double]("cashInvestedAmount").get,
      doc.getAs[Date]("asOfDate").get,
      doc.getAs[Double]("initialMargin").get,
      doc.getAs[Double]("maintenanceMargin").get,
      doc.getAs[Double]("interestRate").get
    )
  }

  implicit object MarginAccountWriter extends BSONDocumentWriter[MarginAccount] {
    def write(marginAccount: MarginAccount) = BSONDocument(
      "depositedFunds" -> marginAccount.depositedFunds,
      "cashInvestedAmount" -> marginAccount.cashInvestedAmount,
      "asOfDate" -> marginAccount.asOfDate,
      "initialMargin" -> marginAccount.initialMargin,
      "maintenanceMargin" -> marginAccount.maintenanceMargin,
      "interestRate" -> marginAccount.interestRate
    )
  }

}