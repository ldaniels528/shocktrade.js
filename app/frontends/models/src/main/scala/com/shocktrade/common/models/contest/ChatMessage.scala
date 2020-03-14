package com.shocktrade.common.models.contest

import java.util.UUID

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents a chat message
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatMessage(val chatID: js.UndefOr[String] = UUID.randomUUID().toString,
                  val userID: js.UndefOr[String],
                  val username: js.UndefOr[String],
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
    def copy(chatID: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined,
             username: js.UndefOr[String] = js.undefined,
             text: js.UndefOr[String] = js.undefined,
             sentTime: js.UndefOr[js.Date] = js.undefined) = new ChatMessage(
      chatID = chatID ?? message.chatID,
      userID = userID ?? message.userID,
      username = username ?? message.username,
      text = text ?? message.text,
      sentTime = sentTime ?? message.sentTime
    )
  }

}