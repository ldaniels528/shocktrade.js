package com.shocktrade.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.ContestActor._
import com.shocktrade.actors.WebSockets._
import com.shocktrade.models.contest.PerkTypes._
import com.shocktrade.models.contest.{Contest, _}
import com.shocktrade.server.trading.{ContestDAO, OrderProcessor}
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

import scala.util.{Failure, Success}

/**
 * Contest I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestActor extends Actor with ActorLogging {

  import context.dispatcher

  override def receive = {
    case CloseOrder(contestId, playerId, orderId, fields) =>
      val mySender = sender()
      ContestDAO.closeOrder(contestId, playerId, orderId) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! OrdersUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }

    case CreateContest(contest) =>
      val mySender = sender()
      ContestDAO.createContest(contest) onComplete {
        case Success(lastError) =>
          WebSockets ! ContestCreated(contest)
          mySender ! lastError
        case Failure(e) => mySender ! e
      }

    case CreateMessage(contestId, message, fields) =>
      val mySender = sender()
      ContestDAO.createMessage(contestId, message) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! MessagesUpdated(_))
        case Failure(e) => mySender ! e
      }

    case CreateOrder(contestId, playerId, order) =>
      val mySender = sender()
      ContestDAO.createOrder(contestId, playerId, order) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! OrdersUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }

    case DeleteContestByID(contestId) =>
      val mySender = sender()
      ContestDAO.deleteContestByID(contestId) onComplete {
        case Success(lastError) =>
          mySender ! lastError
          WebSockets ! ContestDeleted(contestId)
        case Failure(e) => mySender ! e
      }

    case FindContestByID(id, fields) =>
      val mySender = sender()
      ContestDAO.findContestByID(id, fields) onComplete {
        case Success(contest_?) => mySender ! contest_?
        case Failure(e) => mySender ! e
      }

    case FindContests(searchOptions, fields) =>
      val mySender = sender()
      ContestDAO.findContests(searchOptions, fields) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }

    case FindContestsByPlayerID(playerId, fields) =>
      val mySender = sender()
      ContestDAO.findContestsByPlayerID(playerId) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }

    case FindContestsByPlayerName(playerName, fields) =>
      val mySender = sender()
      ContestDAO.findContestsByPlayerName(playerName) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }

    case FindOrderByID(contestId, orderId, fields) =>
      val mySender = sender()
      ContestDAO.findOrderByID(contestId, orderId) onComplete {
        case Success(contest_?) => mySender ! contest_?
        case Failure(e) => mySender ! e
      }

    case JoinContest(contestId, participant) =>
      val mySender = sender()
      ContestDAO.joinContest(contestId, participant) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case ProcessOrders(contest, asOfDate) =>
      processOrders(contest, asOfDate)

    case PurchasePerks(contestId, playerId, perkCodes, totalCost) =>
      val mySender = sender()
      ContestDAO.purchasePerks(contestId, playerId, perkCodes, totalCost) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! PerksUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }

    case QuitContest(contestId, playerId) =>
      val mySender = sender()
      ContestDAO.quitContest(contestId, playerId) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }

    case StartContest(contestId, startTime) =>
      val mySender = sender()
      ContestDAO.startContest(contestId, startTime) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }
  }

  /**
   * Process all active orders
   * @param contest the given [[Contest contest]]
   * @param asOfDate the given effective date
   */
  private def processOrders(contest: Contest, asOfDate: DateTime) {
    // if trading was active during the as-of date
    OrderProcessor.processContest(contest, asOfDate) onComplete {
      case Success(updateCount) =>
        // if an update occurred, notify the users
        if (updateCount > 0) {
          ContestDAO.findContestByID(contest.id, fields = Nil) foreach {
            _ foreach (updatedContest => WebSockets ! ContestUpdated(updatedContest))
          }
        }

      case Failure(e) =>
        log.error(s"An error occur while processing contest '${contest.name}'", e)
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

  case class CreateOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)

  case class DeleteContestByID(contestId: BSONObjectID)

  case class FindContestByID(id: BSONObjectID, fields: Seq[String])

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String])

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String])

  case class FindContestsByPlayerName(playerName: String, fields: Seq[String])

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String])

  case class JoinContest(contestId: BSONObjectID, participant: Participant)

  case class PurchasePerks(contestId: BSONObjectID, playerId: BSONObjectID, perkCodes: Seq[PerkType], totalCost: Double)

  case class ProcessOrders(contest: Contest, asOfDate: DateTime)

  case class QuitContest(contestId: BSONObjectID, playerId: BSONObjectID)

  case class StartContest(contestId: BSONObjectID, startTime: Date)

}