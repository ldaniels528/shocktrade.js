package com.shocktrade.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.JsValue

/**
 * Web Socket Handler
 * @author lawrence.daniels@gmail.com
 */
class WebSocketHandler(out: ActorRef) extends Actor with ActorLogging {
  private val uuid = UUID.randomUUID()

  override def preStart() = WebSockets.register(uuid, self)

  override def postStop() = {
    WebSockets.unregister(uuid)
    ()
  }

  override def receive = {
    case message: JsValue =>
      //log.info(s"Sending message $message")
      out ! message

    case message =>
      log.warning(s"Unhandled message $message")
      unhandled(message)
  }
}

/**
 * Web Socket Handler Singleton
 * @author lawrence.daniels@gmail.com
 */
object WebSocketHandler {

  def props(out: ActorRef) = Props(new WebSocketHandler(out))

}