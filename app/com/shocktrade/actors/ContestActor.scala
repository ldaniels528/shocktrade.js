package com.shocktrade.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.actors.ContestActor._
import com.shocktrade.actors.WebSockets.{ContestCreated, ContestDeleted, ContestUpdated}
import com.shocktrade.controllers.Application.db
import com.shocktrade.models.contest.{Contest, _}
import com.shocktrade.server.trading.{OrderProcessor, TradingDAO}
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Contest I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestActor extends Actor with ActorLogging {
  private implicit val mc = db.collection[BSONCollection]("Contests")
  private implicit val timeout: Timeout = 30.second
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

    case FindContestsByPlayerName(playerName, fields) =>
      val mySender = sender()
      mc.find(BS("participants.name" -> playerName, "status" -> ContestStatus.ACTIVE), fields.toBsonFields).sort(SortFields.toBsonFields)
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
        query = BS("_id" -> contestId, "playerCount" -> BS("$lt" -> Contest.MaxPlayers) /*, "invitationOnly" -> false*/),
        modify = new Update(
          BS("$inc" -> BS("playerCount" -> 1),
            "$addToSet" -> BS("participants" -> participant)), fetchNewObject = true),
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case ProcessOrders(contest, lockExpirationTime, asOfDate) =>
      processOrders(contest, lockExpirationTime, asOfDate)

    case QuitContest(contestId, playerId) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId),
        modify = new Update(
          BS("$inc" -> BS("playerCount" -> -1),
            "$pull" -> BS("participants" -> BS("_id" -> playerId))), fetchNewObject = true),
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case StartContest(contestId, startTime) =>
      val mySender = sender()
      db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId, "startTime" -> BS("$exists" -> false)),
        modify = new Update(BS("$set" -> BS("startTime" -> startTime)), fetchNewObject = true),
        fields = None,
        upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }
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
      if (isSet) q = q ++ BS("perksAllowed" -> true)
    }
    so.robotsAllowed.foreach { isSet =>
      if (isSet) q = q ++ BS("robotsAllowed" -> true)
    }
    q
  }

  private def findContestByID(contestId: BSONObjectID, fields: Seq[String])(implicit mc: BSONCollection, ec: ExecutionContext): Future[Option[Contest]] = {
    mc.find(BS("_id" -> contestId), fields.toBsonFields).cursor[Contest].collect[Seq](1).map(_.headOption)
  }

  private def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(implicit mc: BSONCollection, ec: ExecutionContext): Future[Option[Order]] = {
    findContestByID(contestId, Seq("participants.name", "participants.orders")) map (_ flatMap { contest =>
      contest.participants.flatMap(_.orders.find(_.id == orderId)).headOption
    })
  }

  /**
   * Process all active orders
   * @param contest the given [[Contest contest]]
   * @param asOfDate the given effective date
   */
  private def processOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime) {
    // if trading was active during the as-of date
    OrderProcessor.processContest(contest, asOfDate) onComplete {
      case Success(updateCount) =>
        // if an update occurred, notify the users
        if (updateCount > 0) {
          findContestByID(contest.id, fields = Nil) foreach {
            _ foreach { updatedContest =>
              WebSockets ! ContestUpdated(updatedContest)
            }
          }
        }

        // finally unlock the contest
        unlock(contest, lockExpirationTime)

      case Failure(e) =>
        log.error(s"An error occur while processing contest '${contest.name}'", e)

        // finally unlock the contest
        unlock(contest, lockExpirationTime)
    }
  }

  /**
   * Unlocks the contest
   * @param contest the given [[Contest contest]]
   * @param lockExpirationTime the given lock expiration [[DateTime date]]
   */
  private def unlock(contest: Contest, lockExpirationTime: DateTime): Unit = {
    TradingDAO.unlockContest(contest.id, lockExpirationTime) onComplete {
      case Failure(e) =>
        log.error(e, s"Failed while attempting to unlock Contest '${contest.name}'")
      case Success(_) =>
    }
  }

}

/**
 * Contest Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestActor {

  case class CloseOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class CreateContest(contest: Contest)

  case class CreateMessage(contestId: BSONObjectID, message: Message, fields: Seq[String])

  case class CreateOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order, fields: Seq[String])

  case class DeleteContestByID(contestId: BSONObjectID)

  case class FindContestByID(id: BSONObjectID, fields: Seq[String])

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String])

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String])

  case class FindContestsByPlayerName(playerName: String, fields: Seq[String])

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class JoinContest(contestId: BSONObjectID, participant: Participant)

  case class ProcessOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime)

  case class QuitContest(contestId: BSONObjectID, playerId: BSONObjectID)

  case class StartContest(contestId: BSONObjectID, startTime: Date)

}