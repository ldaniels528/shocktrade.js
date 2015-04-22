package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.functional.syntax._

import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

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
    (JsPath \ "name").read[String] and
      (JsPath \ "facebookID").read[String] and
      (JsPath \ "fundsAvailable").read[BigDecimal] and
      (JsPath \ "score").read[Int] and
      (JsPath \ "lastTradeTime").read[Option[Date]] and
      (JsPath \ "orders").read[List[Order]] and
      (JsPath \ "orderHistory").read[List[Order]] and
      (JsPath \ "positions").read[List[Position]] and
      (JsPath \ "performance").read[List[Performance]] and
      (JsPath \ "_id").read[BSONObjectID])(Participant.apply _)

  implicit val participantWrites: Writes[Participant] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "facebookID").write[String] and
      (JsPath \ "fundsAvailable").write[BigDecimal] and
      (JsPath \ "score").write[Int] and
      (JsPath \ "lastTradeTime").write[Option[Date]] and
      (JsPath \ "orders").write[List[Order]] and
      (JsPath \ "orderHistory").write[List[Order]] and
      (JsPath \ "positions").write[List[Position]] and
      (JsPath \ "performance").write[List[Performance]] and
      (JsPath \ "_id").write[BSONObjectID])(unlift(Participant.unapply))

}
