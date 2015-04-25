package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.actors.ContestActor._
import com.shocktrade.actors.WebSockets.{ContestCreated, ContestUpdated}
import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest._
import com.shocktrade.util.BSONHelper._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Contest I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestActor extends Actor with ActorLogging {
  private val mc = db.collection[BSONCollection]("Contests")
  private val SortFields = Seq("status", "name")

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
          mySender ! lastError
          WebSockets ! ContestCreated(contest)
        case Failure(e) => mySender ! e
      }

    case CreateMessage(contestId, message, fields) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId),
        modify = new Update(BS("$addToSet" -> fields.toBsonFields), fetchNewObject = true),
        fields = Some(BS("messages" -> 1)), upsert = false))
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

    case FindContestByID(id, fields) =>
      val mySender = sender()
      findContestByID(id, fields) onComplete {
        case Success(contest_?) => mySender ! contest_?
        case Failure(e) => mySender ! e
      }

    case FindContests(searchOptions, fields) =>
      val mySender = sender()
      mc.find(createQuery(searchOptions), fields.toBsonFields).sort(SortFields.toBsonFields)
        .cursor[Contest]
        .collect[Seq]() onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }

    case FindContestsByPlayerID(playerId, fields) =>
      val mySender = sender()
      mc.find(BS("participants._id" -> playerId, "status" -> ContestStatus.ACTIVE), fields.toBsonFields).sort(SortFields.toBsonFields)
        .cursor[Contest]
        .collect[Seq]() onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }

    case FindOrderByID(contestId, orderId, fields) =>
      val mySender = sender()
      mc.find(BS("_id" -> contestId, "participants.orders" -> BS("$elemMatch" -> BS("_id" -> orderId))), fields.toBsonFields)
        .cursor[Contest]
        .collect[Seq](1) onComplete {
        case Success(contests) => mySender ! contests.headOption
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

    case message =>
      log.info(s"Unhandled message: $message (${message.getClass.getName})")
      unhandled(message)
  }

  private def findContestByID(contestId: BSONObjectID, fields: Seq[String]): Future[Option[Contest]] = {
    mc.find(BS("_id" -> contestId), fields.toBsonFields).cursor[Contest].collect[Seq](1).map(_.headOption)
  }

  private def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID): Future[Option[Order]] = {
    findContestByID(contestId, Seq("participants.name", "participants.orders")) map (_ flatMap { contest =>
      contest.participants.flatMap(_.orders.find(_.id == orderId)).headOption
    })
  }

  private def createQuery(so: SearchOptions) = {
    var q = BS()
    so.activeOnly.foreach { isSet =>
      if (isSet) q = q ++ BS("status" -> ContestStatus.ACTIVE)
    }
    so.available.foreach { isSet =>
      if (isSet) q = q ++ BS("playerCount" -> BS("$lt" -> Contest.MaxPlayers))
    }
    so.levelCap.foreach { lc =>
      val levelCap = Try(lc.toInt).toOption.getOrElse(0)
      q = q ++ BS("levelCap" -> BS("$gte" -> levelCap))
    }
    so.perksAllowed.foreach { isSet =>
      if (isSet) q = q ++ BS("perksAllowed" -> so.perksAllowed)
    }
    q
  }

}

/**
 * Contest Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestActor {

  trait ContestMutation

  case class CloseOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String]) extends ContestMutation

  case class CreateContest(contest: Contest) extends ContestMutation

  case class CreateMessage(contestId: BSONObjectID, message: Message, fields: Seq[String]) extends ContestMutation

  case class CreateOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order, fields: Seq[String]) extends ContestMutation

  case class FindContestByID(id: BSONObjectID, fields: Seq[String])

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String])

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String])

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class JoinContest(contestId: BSONObjectID, participant: Participant) extends ContestMutation

}