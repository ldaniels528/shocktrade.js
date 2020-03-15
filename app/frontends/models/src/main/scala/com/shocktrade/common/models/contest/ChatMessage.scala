package com.shocktrade.common.models.contest

import java.util.UUID

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents a chat message
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatMessage(val messageID: js.UndefOr[String] = UUID.randomUUID().toString,
                  val userID: js.UndefOr[String],
                  val username: js.UndefOr[String],
                  val message: js.UndefOr[String],
                  val creationTime: js.UndefOr[js.Date] = new js.Date()) extends js.Object

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
      messageID = chatID ?? message.messageID,
      userID = userID ?? message.userID,
      username = username ?? message.username,
      message = text ?? message.message,
      creationTime = sentTime ?? message.creationTime
    )
  }

}