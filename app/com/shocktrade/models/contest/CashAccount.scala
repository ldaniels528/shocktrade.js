package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

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

  implicit val cashAccountReads: Reads[CashAccount] = (
    (__ \ "cashFunds").read[BigDecimal] and
      (__ \ "asOfDate").read[Date])(CashAccount.apply _)

  implicit val cashAccountWrites: Writes[CashAccount] = (
    (__ \ "cashFunds").write[BigDecimal] and
      (__ \ "asOfDate").write[Date])(unlift(CashAccount.unapply))

  implicit object CashAccountReader extends BSONDocumentReader[CashAccount] {
    def read(doc: BSONDocument) = CashAccount(
      doc.getAs[BigDecimal]("cashFunds").get,
      doc.getAs[Date]("asOfDate").get
    )
  }

  implicit object CashAccountWriter extends BSONDocumentWriter[CashAccount] {
    def write(cashAccount: CashAccount) = BSONDocument(
      "cashFunds" -> cashAccount.cashFunds,
      "asOfDate" -> cashAccount.asOfDate
    )
  }

}
