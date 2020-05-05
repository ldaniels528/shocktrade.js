package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a reference to an message
 * @param messageID the given message ID
 */
class MessageRef(val messageID: js.UndefOr[String]) extends js.Object

/**
 * Message Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object MessageRef {

  def apply(messageID: js.UndefOr[String]): MessageRef = new MessageRef(messageID)

  def unapply(ref: MessageRef): Option[String] = ref.messageID.toOption

}