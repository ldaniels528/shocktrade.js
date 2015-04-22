package com.shocktrade.actors

import java.util.UUID

import akka.actor._
import akka.routing.RoundRobinPool
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{obj => JS, _}
import play.libs.Akka

import scala.collection.concurrent.TrieMap

/**
 * Web Socket Relay
 * @author lawrence.daniels@gmail.com
 */
object WebSocketRelay {
  private val actors = TrieMap[UUID, ActorRef]()
  private val system = Akka.system
  private val relayActor = system.actorOf(Props[WebSocketRelayActor].withRouter(RoundRobinPool(nrOfInstances = 50)))

  /**
   * Broadcasts the given message to all connected users
   * @param message the given message
   */
  def !(message: Any) = relayActor ! message

  /**
   * Registers the given actor reference
   * @param uuid the given [[UUID unique identifier]]
   * @param actor the given [[ActorRef]]
   */
  def register(uuid: UUID, actor: ActorRef) = actors(uuid) = actor

  /**
   * Unregisters the given actor reference
   * @param uuid the given [[UUID unique identifier]]
   * @return the option of the [[ActorRef]] being removed
   */
  def unregister(uuid: UUID): Option[ActorRef] = actors.remove(uuid)

  /**
   * Web Socket Relay Actor
   * @author lawrence.daniels@gmail.com
   */
  class WebSocketRelayActor() extends Actor with ActorLogging {
    override def receive = {
      case ContestUpdated(contest) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "contestUpdate", "data" -> contest)
        }

      case QuoteUpdated(quote) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "quoteUpdate", "data" -> quote)
        }

      case message =>
        log.warning(s"Unhandled message $message")
        unhandled(message)
    }
  }

  case class ContestUpdated(contest: JsValue)

  case class QuoteUpdated(quote: JsValue)

}
