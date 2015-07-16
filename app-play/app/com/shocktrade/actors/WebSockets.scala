package com.shocktrade.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.shocktrade.models.contest.{Contest, Participant}
import com.shocktrade.models.profile.UserProfile
import play.api.Logger
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.libs.Akka
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

import scala.collection.concurrent.TrieMap

/**
 * Web Sockets Proxy
 * @author lawrence.daniels@gmail.com
 */
object WebSockets {
  private val actors = TrieMap[UUID, ActorRef]()
  private val system = Akka.system
  private val relayActor = system.actorOf(Props[WsRelayActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "WsRelay")

  /**
   * Broadcasts the given message to all connected users
   * @param message the given message
   */
  def !(message: Any) = relayActor ! message

  /**
   * Registers the given actor reference
   * @param uuid the given [[java.util.UUID unique identifier]]
   * @param actor the given [[akka.actor.ActorRef]]
   */
  def register(uuid: UUID, actor: ActorRef) = {
    Logger.info(s"Registering web socket actor for session # $uuid...")
    actors(uuid) = actor
  }

  /**
   * Unregisters the given actor reference
   * @param uuid the given [[java.util.UUID unique identifier]]
   * @return the option of the [[akka.actor.ActorRef]] being removed
   */
  def unregister(uuid: UUID): Option[ActorRef] = actors.remove(uuid)

  /**
   * Web Socket Relay Actor
   * @author lawrence.daniels@gmail.com
   */
  class WsRelayActor() extends Actor with ActorLogging {
    override def receive = {
      case msg: WsRelayMessage =>
        actors.foreach { case (uid, actor) =>
          actor ! msg.toJsonMessage
        }

      case message =>
        log.warning(s"Unhandled message $message")
        unhandled(message)
    }
  }

  /**
   * Base trait for all Web Socket Replay Messages
   */
  trait WsRelayMessage {
    def toJsonMessage: JsObject
  }

  case class ContestCreated(contest: Contest) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "contest_created", "data" -> contest)
  }

  case class ContestDeleted(id: BSONObjectID) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "contest_deleted", "data" -> JS("id" -> id.stringify))
  }

  case class ContestUpdated(contest: Contest) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "contest_updated", "data" -> contest)
  }

  case class MessagesUpdated(c: Contest) extends WsRelayMessage {
    def toJsonMessage = JS(
      "action" -> "messages_updated",
      "data" -> JS("type" -> "delta", "name" -> c.name, "_id" -> c.id, "messages" -> c.messages))
  }

  case class OrdersUpdated(c: Contest, p: Participant) extends WsRelayMessage {
    def toJsonMessage = JS(
      "action" -> "orders_updated",
      "data" -> JS(
        "type" -> "delta",
        "name" -> c.name, "_id" -> c.id,
        "participants" -> JsArray(Seq(JS("_id" -> p.id, "name" -> p.name, "cashAccount" -> p.cashAccount, "orders" -> p.orders, "closedOrders" -> p.closedOrders)))
      ))
  }

  case class ParticipantUpdated(c: Contest, p: Participant) extends WsRelayMessage {
    def toJsonMessage = JS(
      "action" -> "participant_updated",
      "data" -> JS(
        "type" -> "delta",
        "name" -> c.name, "_id" -> c.id,
        "participants" -> Seq(p))
    )
  }

  case class PerksUpdated(c: Contest, p: Participant) extends WsRelayMessage {
    def toJsonMessage = JS(
      "action" -> "perks_updated",
      "data" -> JS(
        "type" -> "delta",
        "name" -> c.name, "_id" -> c.id,
        "participants" -> JsArray(Seq(JS("_id" -> p.id, "name" -> p.name, "cashAccount" -> p.cashAccount, "perks" -> p.perks)))
      ))
  }

  case class QuoteUpdated(quote: JsValue) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "quote_updated", "data" -> quote)
  }

  case class UserProfileUpdated(profile: UserProfile) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "profile_updated", "data" -> profile)
  }

  case class UserStateChanged(userID: String, connected: Boolean) extends WsRelayMessage {
    def toJsonMessage = JS("action" -> "user_status_changed", "data" -> JS("userID" -> userID, "connected" -> connected))
  }

}
