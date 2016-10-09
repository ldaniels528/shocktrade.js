package com.shocktrade.common.models.contest

import java.util.UUID

import com.shocktrade.common.models.PlayerRef
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a chat message
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ChatMessage(val _id: js.UndefOr[String] = UUID.randomUUID().toString,
                  val sender: js.UndefOr[PlayerRef],
                  val text: js.UndefOr[String],
                  val sentTime: js.UndefOr[js.Date] = new js.Date()) extends js.Object

/**
  * Chat Message Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ChatMessage {

  /**
    * Chat Message Enrichment
    * @param message the given [[ChatMessage chat message]]
    */
  implicit class ChatMessageEnrichment(val message: ChatMessage) extends AnyVal {

    @inline
    def copy(_id: js.UndefOr[String] = js.undefined,
             sender: js.UndefOr[PlayerRef] = js.undefined,
             text: js.UndefOr[String] = js.undefined,
             sentTime: js.UndefOr[js.Date] = js.undefined) = {
      new ChatMessage(
        _id = _id ?? message._id,
        sender = sender ?? message.sender,
        text = text ?? message.text,
        sentTime = sentTime ?? message.sentTime
      )
    }

  }

}