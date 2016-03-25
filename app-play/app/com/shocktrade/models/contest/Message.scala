package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats.{BSONObjectIDFormat => BSONIDF}
import reactivemongo.bson.{BSONObjectID, Macros}

/**
  * Represents a contest chat message
  * @author lawrence.daniels@gmail.com
  */
case class Message(id: BSONObjectID = BSONObjectID.generate,
                   sender: PlayerRef,
                   text: String,
                   recipient: Option[PlayerRef] = None,
                   sentTime: Date = new Date())

/**
  * Message Singleton
  * @author lawrence.daniels@gmail.com
  */
object Message {

  /**
    * {
    * "_id" : ObjectId("534208d6f09e3fe069ee34ff"),
    * "sentTime" : ISODate("2014-04-07T02:09:26.507Z"),
    * "sender" : {
    * "_id" : ObjectId("534208d6f09e3fe069ee34ee"),
    * "name" : "gadget",
    * "facebookID" : "100002058615115"
    * },
    * "recipient" : null,
    * "text" : "Hello"
    * }
    **/
  implicit val MessageFormat = Json.format[Message]

  implicit val MessageHandler = Macros.handler[Message]

}
