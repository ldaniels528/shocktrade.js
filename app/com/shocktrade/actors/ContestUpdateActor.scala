package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.actors.ContestUpdateActor._
import com.shocktrade.actors.WebSockets.{ContestCreated, ContestDeleted, ContestUpdated}
import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest.{Contest, _}
import com.shocktrade.util.BSONHelper._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Contest Update Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestUpdateActor extends Actor with ActorLogging {
  private implicit val mc = db.collection[BSONCollection]("Contests")

  import context.dispatcher

  override def receive = {

    case CloseOrder(contestId, playerId, orderId, fields) =>
      val mySender = sender()
      (for {
        order <- findOrderByID(contestId, orderId) map (_ orDie s"Order not found")
        contest_? <- db.command(FindAndModify(
          collection = "Contests",
          query = BS("_id" -> contestId, "participants._id" -> playerId),
          modify = new Update(BS(
            "$pull" -> BS("participants.$.orders" -> BS("_id" -> orderId)),
            "$addToSet" -> BS("participants.$.orderHistory" -> order)),
            fetchNewObject = true),
          fields = Some(fields.toBsonFields),
          upsert = false))
      } yield contest_?) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case CreateContest(contest) =>
      val mySender = sender()
      mc.insert(contest) onComplete {
        case Success(lastError) =>
          WebSockets ! ContestCreated(contest)
          mySender ! lastError
        case Failure(e) => mySender ! e
      }

    case CreateMessage(contestId, message, fields) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId),
        modify = new Update(BS("$addToSet" -> BS("messages" -> message)), fetchNewObject = true),
        fields = Some(fields.toBsonFields), upsert = false))
        .map(_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case CreateOrder(contestId, playerId, order, fields) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId, "participants._id" -> playerId),
        modify = new Update(BS("$addToSet" -> BS("participants.$.orders" -> order)), fetchNewObject = true),
        fields = Some(fields.toBsonFields),
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case DeleteContestByID(contestId) =>
      val mySender = sender()
      mc.remove(query = BS("_id" -> contestId), firstMatchOnly = true) onComplete {
        case Success(lastError) =>
          mySender ! lastError
          WebSockets ! ContestDeleted(contestId)
        case Failure(e) => mySender ! e
      }

    case JoinContest(contestId, participant) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId),
        modify = new Update(BS("$addToSet" -> BS("participants" -> participant)), fetchNewObject = true),
        fields = None,
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case QuitContest(contestId, playerId) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId),
        modify = new Update(BS("$pull" -> BS("participants._id" -> playerId)), fetchNewObject = true),
        fields = None,
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

  }

  private def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(implicit mc: BSONCollection, ec: ExecutionContext): Future[Option[Order]] = {
    ContestReaderActor.findContestByID(contestId, Seq("participants.name", "participants.orders")) map (_ flatMap { contest =>
      contest.participants.flatMap(_.orders.find(_.id == orderId)).headOption
    })
  }

}

/**
 * Contest Update Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestUpdateActor {

  case class CloseOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class CreateContest(contest: Contest)

  case class CreateMessage(contestId: BSONObjectID, message: Message, fields: Seq[String])

  case class CreateOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order, fields: Seq[String])

  case class DeleteContestByID(contestId: BSONObjectID)

  case class JoinContest(contestId: BSONObjectID, participant: Participant)

  case class QuitContest(contestId: BSONObjectID, playerId: BSONObjectID)

}