package com.shocktrade.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.controllers.ContestResources.db
import com.shocktrade.models.contest.{Contest, ContestStatus}
import com.shocktrade.util.BSONHelper._
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.libs.Akka
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.Future
import scala.util.Try

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val reader = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 50)))
  private val writer = system.actorOf(Props[ContestActor])
  private val mcC = db.collection[JSONCollection]("Contests")

  private val DisplayColumns =
    JS("name" -> 1, "creator" -> 1, "startTime" -> 1, "expirationTime" -> 1, "startingBalance" -> 1, "status" -> 1,
      "ranked" -> 1, "playerCount" -> 1, "levelCap" -> 1, "perksAllowed" -> 1, "maxParticipants" -> 1,
      "participants.name" -> 1, "participants.facebookID" -> 1)
  private val SortFields = JS("status" -> 1, "name" -> 1)

  /**
   * Allows a user/process to "tell" the actor some fact or process a command
   * @param message the given command or fact
   */
  def !(message: Any): Unit = {
    message match {
      case msg: UpdateMessage => writer ! msg
      case msg => reader ! msg
    }
  }

  /**
   * Allows a user/process to "ask" the actor a question
   * @param message the given question
   * @param timeout the timeout duration
   * @return the response message
   */
  def ?(message: Any)(implicit timeout: Timeout): Future[Any] = {
    import akka.pattern.ask

    reader ? message
  }

  /**
   * Contest Actor
   * @author lawrence.daniels@gmail.com
   */
  class ContestActor extends Actor with ActorLogging {
    override def receive = {
      case CloseOrder(contestId, playerName, orderId, order, fields) =>
        val mySender = sender()
        db.command(FindAndModify(
          collection = "Contests",
          query = BS("_id" -> contestId, "participants.name" -> playerName),
          modify = new Update(BS(
            "$pull" -> BS("participants.$.orders" -> BS("_id" -> orderId)),
            "$addToSet" -> BS("participants.$.orderHistory" -> order.toBson)),
            fetchNewObject = true),
          fields = Some(fields.bsonFields),
          upsert = false)) map (Json.toJson(_)) foreach (mySender ! _) // ~> Option[JsValue]

      case CreateContest(contest) =>
        mcC.insert(contest.toJson) foreach { lastError =>
          log.info(s"lastError = $lastError")
          WebSocketRelay ! WebSocketRelay.ContestUpdated(contest.toJson)
        }

      case CreateOrder(contestId, playerName, order, fields) =>
        val mySender = sender()
        db.command(FindAndModify(
          collection = "Contests",
          query = BS("_id" -> contestId, "participants.name" -> playerName),
          modify = new Update(BS("$addToSet" -> BS("participants.$.orders" -> order.toBson)), fetchNewObject = true),
          fields = Some(fields.bsonFields),
          upsert = false)) map (Json.toJson(_)) foreach (mySender ! _) // ~> Option[JsValue]

      case FindContestByID(id, fields) =>
        val mySender = sender()
        getContestByID(id, fields.jsonFields).foreach(mySender ! _.headOption) // ~> Option[JsObject]

      case FindContests(searchOptions) =>
        val mySender = sender()
        mcC.find(createQuery(searchOptions), DisplayColumns).sort(SortFields)
          .cursor[JsObject]
          .collect[Seq]().foreach(mySender ! JsArray(_)) // ~> JsArray

      case FindContestsByPlayer(playerName) =>
        val mySender = sender()
        mcC.find(JS("participants.name" -> playerName, "status" -> ContestStatus.ACTIVE.name), DisplayColumns).sort(SortFields)
          .cursor[JsObject]
          .collect[Seq]().foreach(mySender ! JsArray(_)) // ~> JsArray

      case FindOrderByID(contestId, orderId, fields) =>
        val mySender = sender()
        mcC.find(JS("_id" -> contestId, "participants.orders" -> JS("$elemMatch" -> JS("_id" -> orderId))), fields.jsonFields)
          .cursor[JsObject]
          .collect[Seq]().foreach(mySender ! _.headOption) // ~> Option[JsObject]

      case message@SendMessage(contestId, sentBy, text, sentTime) =>
        val mySender = sender()
        db.command(FindAndModify(
          collection = "Contests",
          query = BS("_id" -> contestId),
          modify = new Update(BS("$addToSet" -> BS("messages" -> message.toBson)), fetchNewObject = true),
          fields = Some(BS("messages" -> 1)), upsert = false))
          .map(_ map (Json.toJson(_))) foreach (mySender ! _) // ~> Option[JsObject]

      case message =>
        log.info(s"Unhandled message: $message (${message.getClass.getName})")
        unhandled(message)
    }

    private def getContestByID(id: BSONObjectID, fields: JsObject): Future[Seq[JsObject]] = {
      mcC.find(JS("_id" -> id), fields).cursor[JsObject].collect[Seq](1).mapTo[Seq[JsObject]]
    }

    private def createQuery(so: SearchOptions): JsObject = {
      val levelCap = Option(so.levelCap).flatMap(s => Try(s.toInt).toOption).getOrElse(0)
      var q = JS()
      if (so.activeOnly) q = q ++ JS("status" -> ContestStatus.ACTIVE.name)
      if (so.available) q = q ++ JS("playerCount" -> JS("$lt" -> Contest.MaxPlayers))
      if (so.levelCapAllowed) q = q ++ JS("levelCap" -> JS("$gte" -> levelCap))
      if (so.perksAllowed) q = q ++ JS("$or" -> JsArray(Seq(JS("perksAllowed" -> so.perksAllowed), JS("perksAllowed" -> JS("$exists" -> false)))))
      q
    }

  }

  trait UpdateMessage

  case class CloseOrder(contestId: BSONObjectID, playerName: String, orderId: BSONObjectID, order: JsObject, fields: Seq[String] = Nil)
    extends UpdateMessage

  case class CreateContest(contest: Contest) extends UpdateMessage

  case class CreateOrder(contestId: BSONObjectID, playerName: String, order: JsObject, fields: Seq[String] = Nil)

  case class FindContestByID(id: BSONObjectID, fields: Seq[String] = Nil)

  case class GetContestOrders(id: BSONObjectID, playerName: String)

  case class FindContestsByPlayer(playerName: String)

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String] = Nil)

  case class FindContests(searchOptions: SearchOptions)

  case class SendMessage(contestId: BSONObjectID, sentBy: String, text: String, sentTime: Date = new Date())
    extends UpdateMessage {
    def toBson = BS(
      "_id" -> BSONObjectID.generate,
      "sender" -> BS("name" -> sentBy),
      "sentTime" -> new BSONDateTime(sentTime.getTime),
      "text" -> text
    )
  }

  case class SearchOptions(activeOnly: Boolean,
                           available: Boolean,
                           perksAllowed: Boolean,
                           friendsOnly: Boolean,
                           acquaintances: Boolean,
                           levelCap: String,
                           levelCapAllowed: Boolean)

}