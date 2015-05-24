package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.PerkTypes.PerkType
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
case class Participant(id: BSONObjectID = BSONObjectID.generate,
                       name: String,
                       facebookId: String,
                       fundsAvailable: BigDecimal,
                       lastTradeTime: Option[Date] = None,
                       marginAccount: Option[MarginAccount] = None,
                       orders: List[Order] = Nil,
                       orderHistory: List[ClosedOrder] = Nil,
                       perks: List[PerkType] = Nil,
                       positions: List[Position] = Nil,
                       performance: List[Performance] = Nil)

/**
 * Participant Singleton
 * @author lawrence.daniels@gmail.com
 */
object Participant {

  implicit val participantReads: Reads[Participant] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "fundsAvailable").read[BigDecimal] and
      (__ \ "lastTradeTime").readNullable[Date] and
      (__ \ "marginAccount").readNullable[MarginAccount] and
      (__ \ "orders").readNullable[List[Order]].map(_.getOrElse(Nil)) and
      (__ \ "orderHistory").readNullable[List[ClosedOrder]].map(_.getOrElse(Nil)) and
      (__ \ "perks").read[List[PerkType]] and
      (__ \ "positions").readNullable[List[Position]].map(_.getOrElse(Nil)) and
      (__ \ "performance").readNullable[List[Performance]].map(_.getOrElse(Nil)))(Participant.apply _)

  implicit val participantWrites: Writes[Participant] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "facebookID").write[String] and
      (__ \ "fundsAvailable").write[BigDecimal] and
      (__ \ "lastTradeTime").writeNullable[Date] and
      (__ \ "marginAccount").writeNullable[MarginAccount] and
      (__ \ "orders").write[List[Order]] and
      (__ \ "orderHistory").write[List[ClosedOrder]] and
      (__ \ "perks").write[List[PerkType]] and
      (__ \ "positions").write[List[Position]] and
      (__ \ "performance").write[List[Performance]])(unlift(Participant.unapply))

  implicit object ParticipantReader extends BSONDocumentReader[Participant] {
    def read(doc: BSONDocument) = Participant(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get,
      doc.getAs[BigDecimal]("fundsAvailable").get,
      doc.getAs[Date]("lastTradeTime"),
      doc.getAs[MarginAccount]("marginAccount"),
      doc.getAs[List[Order]]("orders").getOrElse(Nil),
      doc.getAs[List[ClosedOrder]]("orderHistory").getOrElse(Nil),
      doc.getAs[List[PerkType]]("perks").getOrElse(Nil),
      doc.getAs[List[Position]]("positions").getOrElse(Nil),
      doc.getAs[List[Performance]]("performance").getOrElse(Nil)
    )
  }

  implicit object ParticipantWriter extends BSONDocumentWriter[Participant] {
    def write(participant: Participant) = BSONDocument(
      "_id" -> participant.id,
      "name" -> participant.name,
      "facebookID" -> participant.facebookId,
      "fundsAvailable" -> participant.fundsAvailable,
      "lastTradeTime" -> participant.lastTradeTime,
      "marginAccount" -> participant.marginAccount,
      "orders" -> participant.orders,
      "orderHistory" -> participant.orderHistory,
      "perks" -> participant.perks,
      "positions" -> participant.positions,
      "performance" -> participant.performance
    )
  }

}
