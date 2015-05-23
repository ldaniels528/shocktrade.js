package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.ContestStatuses._
import com.shocktrade.models.contest.OrderTypes.OrderType
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a trading commission
 * @author lawrence.daniels@gmail.com
 */
case class Commission(paid: BigDecimal, paidDate: Date, orderType: OrderType)

/**
 * Commission Singleton
 * @author lawrence.daniels@gmail.com
 */
object Commission {

  implicit val commissionReads: Reads[Commission] = (
    (__ \ "paid").read[BigDecimal] and
      (__ \ "paidDate").read[Date] and
      (__ \ "orderType").read[OrderType])(Commission.apply _)

  implicit val commissionWrites: Writes[Commission] = (
    (__ \ "paid").write[BigDecimal] and
      (__ \ "paidDate").write[Date] and
      (__ \ "orderType").write[OrderType])(unlift(Commission.unapply))

  implicit object CommissionReader extends BSONDocumentReader[Commission] {
    def read(doc: BSONDocument) = Commission(
      doc.getAs[BigDecimal]("paid").get,
      doc.getAs[Date]("paidDate").get,
      doc.getAs[OrderType]("orderType").get
    )
  }

  implicit object CommissionWriter extends BSONDocumentWriter[Commission] {
    def write(commission: Commission) = BSONDocument(
      "paid" -> commission.paid,
      "paidDate" -> commission.paidDate,
      "orderType" -> commission.orderType
    )
  }

}
