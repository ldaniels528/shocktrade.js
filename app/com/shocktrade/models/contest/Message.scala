package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
 * Represents a contest chat message
 * @author lawrence.daniels@gmail.com
 */
case class Message(id: BSONObjectID = BSONObjectID.generate,
                   sentBy: Player,
                   text: String,
                   recipient: Option[Player] = None,
                   sentTime: Date = new Date())

/**
 * Message Singleton
 * @author lawrence.daniels@gmail.com
 */
object Message {

  /**
   * {
			"_id" : ObjectId("534208d6f09e3fe069ee34ff"),
			"sentTime" : ISODate("2014-04-07T02:09:26.507Z"),
			"sender" : {
				"name" : "gadget",
				"facebookID" : "100002058615115"
			},
			"recipient" : null,
			"text" : "Hello"
   * }
   **/
  implicit val messageReads: Reads[Message] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "sender").read[Player] and
      (__ \ "text").read[String] and
      (__ \ "recipient").readNullable[Player] and
      (__ \ "sentTime").read[Date])(Message.apply _)

  implicit val messageWrites: Writes[Message] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "sender").write[Player] and
      (__ \ "text").write[String] and
      (__ \ "recipient").writeNullable[Player] and
      (__ \ "sentTime").write[Date])(unlift(Message.unapply))

  implicit object MessageReader extends BSONDocumentReader[Message] {
    def read(doc: BSONDocument) = Message(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[Player]("sender").get,
      doc.getAs[String]("text").get,
      doc.getAs[Player]("recipient"),
      doc.getAs[Date]("sentTime").get
    )
  }

  implicit object MessageWriter extends BSONDocumentWriter[Message] {
    def write(message: Message) = BSONDocument(
      "_id" -> message.id,
      "sender" -> message.sentBy,
      "text" -> message.text,
      "recipient" -> message.recipient,
      "sentTime" -> message.sentTime
    )
  }

}
