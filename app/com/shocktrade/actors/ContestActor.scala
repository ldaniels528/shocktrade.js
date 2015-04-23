package com.shocktrade.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.ContestActor._
import com.shocktrade.actors.WebSocketRelay.ContestUpdated
import com.shocktrade.controllers.Application.db
import com.shocktrade.models.contest.{Contest, ContestStatus, Order, SearchOptions}
import com.shocktrade.util.BSONHelper._
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.Reads._
import play.api.libs.json.{JsArray, JsObject, Json}
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.Future
import scala.util.Try

/**
 * Contest Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestActor extends Actor with ActorLogging {
  private val mcCB = db.collection[BSONCollection]("Contests")
  private val mcCJ = db.collection[JSONCollection]("Contests")
  private val SortFields = JS("status" -> 1, "name" -> 1)

  import context.dispatcher

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
        fields = Some(fields.toBsonFields),
        upsert = false)) map (Json.toJson(_)) foreach (mySender ! _) // ~> Option[JsValue]

    case CreateContest(contest) =>
      val mySender = sender()
      mcCB.insert(contest) foreach { lastError =>
        mySender ! lastError
        WebSocketRelay ! ContestUpdated(Json.toJson(contest))
      }

    case CreateOrder(contestId, playerName, order, fields) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId, "participants.name" -> playerName),
        modify = new Update(BS("$addToSet" -> BS("participants.$.orders" -> order)), fetchNewObject = true),
        fields = Some(fields.toBsonFields),
        upsert = false)) map (Json.toJson(_)) foreach (mySender ! _) // ~> Option[JsValue]

    case FindContestByID(id, fields) =>
      val mySender = sender()
      getContestByID(id, fields.toJsonFields).foreach(mySender ! _.headOption) // ~> Option[JsObject]

    case FindContests(searchOptions, fields) =>
      val mySender = sender()
      mcCJ.find(createQuery(searchOptions), fields.toJsonFields).sort(SortFields)
        .cursor[JsObject]
        .collect[Seq]().foreach(mySender ! _) // ~> Seq[JsValue]

    case FindContestsByPlayerName(playerName, fields) =>
      val mySender = sender()
      mcCJ.find(JS("participants.name" -> playerName, "status" -> ContestStatus.ACTIVE), fields.toJsonFields).sort(SortFields)
        .cursor[JsObject]
        .collect[Seq]().foreach(mySender ! _) // ~> Seq[JsValue]

    case FindContestsByPlayerID(playerId, fields) =>
      val mySender = sender()
      mcCJ.find(JS("participants._id" -> playerId, "status" -> ContestStatus.ACTIVE), fields.toJsonFields).sort(SortFields)
        .cursor[JsObject]
        .collect[Seq]().foreach(mySender ! _) // ~> Seq[JsValue]

    case FindOrderByID(contestId, orderId, fields) =>
      val mySender = sender()
      mcCJ.find(JS("_id" -> contestId, "participants.orders" -> JS("$elemMatch" -> JS("_id" -> orderId))), fields.toJsonFields)
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
    mcCJ.find(JS("_id" -> id), fields).cursor[JsObject].collect[Seq](1).mapTo[Seq[JsObject]]
  }

  private def createQuery(so: SearchOptions): JsObject = {
    var q = JS()
    if (so.activeOnly) q = q ++ JS("status" -> ContestStatus.ACTIVE)
    if (so.available) q = q ++ JS("playerCount" -> JS("$lt" -> Contest.MaxPlayers))
    if (so.levelCapAllowed) {
      val levelCap = Try(so.levelCap.map(_.toInt)).toOption.flatten.getOrElse(0)
      q = q ++ JS("levelCap" -> JS("$gte" -> levelCap))
    }
    if (so.perksAllowed) q = q ++ JS("$or" -> JsArray(Seq(JS("perksAllowed" -> so.perksAllowed), JS("perksAllowed" -> JS("$exists" -> false)))))
    q
  }

}

/**
 * Contest Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestActor {

  case class CloseOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, order: JsObject, fields: Seq[String])

  case class CreateContest(contest: Contest)

  case class CreateOrder(contestId: BSONObjectID, playerName: String, order: Order, fields: Seq[String])

  case class FindContestByID(id: BSONObjectID, fields: Seq[String])

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String])

  case class FindContestsByPlayerName(playerName: String, fields: Seq[String])

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String])

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class FindOrders(id: BSONObjectID, playerName: String)

  case class SendMessage(contestId: BSONObjectID, sentBy: String, text: String, sentTime: Date) {
    def toBson = BS(
      "_id" -> BSONObjectID.generate,
      "sender" -> BS("name" -> sentBy),
      "sentTime" -> new BSONDateTime(sentTime.getTime),
      "text" -> text
    )
  }

}