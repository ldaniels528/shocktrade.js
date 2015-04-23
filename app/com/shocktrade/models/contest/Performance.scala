package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
 * Represents the performance associated with the sell of a position
 * @author lawrence.daniels@gmail.com
 */
case class Performance(symbol: String,
                       exchange: String,
                       pricePaid: BigDecimal,
                       priceSold: BigDecimal,
                       quantity: Int,
                       commissions: List[Commission] = Nil,
                       purchasedDate: Date,
                       soldDate: Date,
                       id: BSONObjectID = BSONObjectID.generate)

/**
 * Performance Singleton
 * @author lawrence.daniels@gmail.com
 */
object Performance {

  implicit val performanceReads: Reads[Performance] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "pricePaid").read[BigDecimal] and
      (__ \ "priceSold").read[BigDecimal] and
      (__ \ "quantity").read[Int] and
      (__ \ "commissions").read[List[Commission]] and
      (__ \ "purchasedDate").read[Date] and
      (__ \ "soldDate").read[Date] and
      (__ \ "_id").read[BSONObjectID])(Performance.apply _)

  implicit val performanceWrites: Writes[Performance] = (
    (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "pricePaid").write[BigDecimal] and
      (__ \ "priceSold").write[BigDecimal] and
      (__ \ "quantity").write[Int] and
      (__ \ "commissions").write[List[Commission]] and
      (__ \ "purchasedDate").write[Date] and
      (__ \ "soldDate").write[Date] and
      (__ \ "_id").write[BSONObjectID])(unlift(Performance.unapply))

  implicit object PerformanceReader extends BSONDocumentReader[Performance] {
    def read(doc: BSONDocument) = Performance(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[BigDecimal]("pricePaid").get,
      doc.getAs[BigDecimal]("priceSold").get,
      doc.getAs[Int]("quantity").get,
      doc.getAs[List[Commission]]("commissions").get,
      doc.getAs[Date]("purchasedDate").get,
      doc.getAs[Date]("soldDate").get,
      doc.getAs[BSONObjectID]("_id").get
    )
  }

  implicit object PerformanceWriter extends BSONDocumentWriter[Performance] {
    def write(performance: Performance) = BSONDocument(
      "_id" -> performance.id,
      "symbol" -> performance.symbol,
      "exchange" -> performance.exchange,
      "pricePaid" -> performance.pricePaid,
      "priceSold" -> performance.priceSold,
      "quantity" -> performance.quantity,
      "commissions" -> performance.commissions,
      "purchasedDate" -> performance.purchasedDate,
      "soldDate" -> performance.soldDate
    )
  }

}

