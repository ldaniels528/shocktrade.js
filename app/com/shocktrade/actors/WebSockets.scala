package com.shocktrade.actors

import java.util.UUID

import akka.actor._
import akka.routing.RoundRobinPool
import com.shocktrade.models.contest.Contest
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{obj => JS, _}
import play.libs.Akka
import reactivemongo.bson.BSONObjectID

import scala.collection.concurrent.TrieMap

/**
 * Web Sockets Proxy
 * @author lawrence.daniels@gmail.com
 */
object WebSockets {
  private val actors = TrieMap[UUID, ActorRef]()
  private val system = Akka.system
  private val relayActor = system.actorOf(Props[WebSocketRelayActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "WebSocketRelay")

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
  def register(uuid: UUID, actor: ActorRef) = {
    Logger.info(s"Registering web socket actor for session # $uuid...")
    actors(uuid) = actor
  }

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
      case ContestCreated(contest) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "contest_created", "data" -> contest)
        }

      case ContestDeleted(id) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "contest_deleted", "data" -> JS("id" -> id.stringify))
        }

      case ContestUpdated(contest) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "contest_updated", "data" -> contest)
        }

      case QuoteUpdated(quote) =>
        actors.foreach { case (uid, actor) =>
          actor ! JS("action" -> "quote_updated", "data" -> quote)
        }

      case message =>
        log.warning(s"Unhandled message $message")
        unhandled(message)
    }
  }

  case class ContestCreated(contest: Contest)

  case class ContestDeleted(id: BSONObjectID)

  case class ContestUpdated(contest: Contest)

  case class QuoteUpdated(quote: JsValue)

}
