package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a contest chat message
 * @author lawrence.daniels@gmail.com
 */
case class Message(sender: String,
                   recipient: Option[String] = None,
                   sentTime: Date,
                   text: String,
                   id: BSONObjectID = BSONObjectID.generate)

/**
 * Message Singleton
 * @author lawrence.daniels@gmail.com
 */
object Message {

  implicit val messageReads: Reads[Message] = (
    (JsPath \ "sender").read[String] and
      (JsPath \ "recipient").read[Option[String]] and
      (JsPath \ "sentTime").read[Date] and
      (JsPath \ "text").read[String] and
      (JsPath \ "_id").read[BSONObjectID])(Message.apply _)

  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "sender").write[String] and
      (JsPath \ "recipient").write[Option[String]] and
      (JsPath \ "sentTime").write[Date] and
      (JsPath \ "text").write[String] and
      (JsPath \ "_id").write[BSONObjectID])(unlift(Message.unapply))

}
