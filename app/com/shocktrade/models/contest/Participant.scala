package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
 * Represents a contest participant
 * @author lawrence.daniels@gmail.com
 */
case class Participant(name: String,
                       facebookId: String,
                       fundsAvailable: BigDecimal,
                       score: Int = 0,
                       lastTradeTime: Option[Date] = None,
                       orders: List[Order] = Nil,
                       orderHistory: List[Order] = Nil,
                       positions: List[Position] = Nil,
                       performance: List[Performance] = Nil,
                       id: BSONObjectID = BSONObjectID.generate)

/**
 * Participant Singleton
 * @author lawrence.daniels@gmail.com
 */
object Participant {

  implicit val participantReads: Reads[Participant] = (
    (__ \ "name").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "fundsAvailable").read[BigDecimal] and
      (__ \ "score").read[Int] and
      (__ \ "lastTradeTime").read[Option[Date]] and
      (__ \ "orders").read[List[Order]] and
      (__ \ "orderHistory").read[List[Order]] and
      (__ \ "positions").read[List[Position]] and
      (__ \ "performance").read[List[Performance]] and
      (__ \ "_id").read[BSONObjectID])(Participant.apply _)

  implicit val participantWrites: Writes[Participant] = (
    (__ \ "name").write[String] and
      (__ \ "facebookID").write[String] and
      (__ \ "fundsAvailable").write[BigDecimal] and
      (__ \ "score").write[Int] and
      (__ \ "lastTradeTime").write[Option[Date]] and
      (__ \ "orders").write[List[Order]] and
      (__ \ "orderHistory").write[List[Order]] and
      (__ \ "positions").write[List[Position]] and
      (__ \ "performance").write[List[Performance]] and
      (__ \ "_id").write[BSONObjectID])(unlift(Participant.unapply))

  implicit object ParticipantReader extends BSONDocumentReader[Participant] {
    def read(doc: BSONDocument) = Participant(
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get,
      doc.getAs[BigDecimal]("fundsAvailable").get,
      doc.getAs[Int]("score").get,
      doc.getAs[Date]("lastTradeTime"),
      doc.getAs[List[Order]]("orders").get,
      doc.getAs[List[Order]]("orderHistory").get,
      doc.getAs[List[Position]]("positions").get,
      doc.getAs[List[Performance]]("performance").get,
      doc.getAs[BSONObjectID]("_id").get
    )
  }

  implicit object ParticipantWriter extends BSONDocumentWriter[Participant] {
    def write(participant: Participant) = BSONDocument(
      "_id" -> participant.id,
      "name" -> participant.name,
      "facebookID" -> participant.facebookId,
      "fundsAvailable" -> participant.fundsAvailable,
      "score" -> participant.score,
      "lastTradeTime" -> participant.lastTradeTime,
      "orders" -> participant.orders,
      "orderHistory" -> participant.orderHistory,
      "positions" -> participant.positions,
      "performance" -> participant.performance
    )
  }

}
