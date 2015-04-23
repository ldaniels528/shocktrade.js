package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{__, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
 * Represents a financial position
 * @author lawrence.daniels@gmail.com
 */
case class Position(symbol: String,
                    exchange: String,
                    pricePaid: BigDecimal,
                    quantity: Int,
                    commission: Commission,
                    purchasedDate: Date,
                    id: BSONObjectID = BSONObjectID.generate)

/**
 * Position Singleton
 * @author lawrence.daniels@gmail.com
 */
object Position {

  implicit val positionReads: Reads[Position] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "pricePaid").read[BigDecimal] and
      (__ \ "quantity").read[Int] and
      (__ \ "commission").read[Commission] and
      (__ \ "purchasedDate").read[Date] and
      (__ \ "_id").read[BSONObjectID])(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "pricePaid").write[BigDecimal] and
      (__ \ "quantity").write[Int] and
      (__ \ "commission").write[Commission] and
      (__ \ "purchasedDate").write[Date] and
      (__ \ "_id").write[BSONObjectID])(unlift(Position.unapply))

  implicit object PositionReader extends BSONDocumentReader[Position] {
    def read(doc: BSONDocument) = Position(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[BigDecimal]("pricePaid").get,
      doc.getAs[Int]("quantity").get,
      doc.getAs[Commission]("commission").get,
      doc.getAs[Date]("purchasedDate").get,
      doc.getAs[BSONObjectID]("_id").get
    )
  }

  implicit object PositionWriter extends BSONDocumentWriter[Position] {
    def write(position: Position) = BSONDocument(
      "_id" -> position.id,
      "symbol" -> position.symbol,
      "exchange" -> position.exchange,
      "pricePaid" -> position.pricePaid,
      "quantity" -> position.quantity,
      "commission" -> position.commission,
      "purchasedDate" -> position.purchasedDate
    )
  }

}
