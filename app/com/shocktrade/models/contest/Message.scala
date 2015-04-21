package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper
import BSONHelper._
import play.api.libs.json.Json.{obj => JS, _}
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
                   id: Option[BSONObjectID] = None) {

  def toJson = JS(
    "_id" -> id.toBSID,
    "recipient" -> recipient,
    "sender" -> sender,
    "sentTime" -> sentTime,
    "text" -> text
  )

}
