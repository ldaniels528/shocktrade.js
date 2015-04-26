package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.ContestReaderActor._
import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest._
import com.shocktrade.util.BSONHelper._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Contest I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestReaderActor extends Actor with ActorLogging {
  private implicit val mc = db.collection[BSONCollection]("Contests")
  private val SortFields = Seq("status", "name")

  import context.dispatcher

  override def receive = {

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

    case message =>
      log.info(s"Unhandled message: $message (${message.getClass.getName})")
      unhandled(message)
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
object ContestReaderActor {

  private[actors] def findContestByID(contestId: BSONObjectID, fields: Seq[String])(implicit mc: BSONCollection, ec: ExecutionContext): Future[Option[Contest]] = {
    mc.find(BS("_id" -> contestId), fields.toBsonFields).cursor[Contest].collect[Seq](1).map(_.headOption)
  }

  case class FindContestByID(id: BSONObjectID, fields: Seq[String])

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String])

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String])

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

}