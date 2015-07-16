package com.shocktrade.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.shocktrade.actors.ContestActor._
import com.shocktrade.actors.WebSockets._
import com.shocktrade.models.contest.AccountTypes.AccountType
import com.shocktrade.models.contest.PerkTypes._
import com.shocktrade.models.contest.{Contest, _}
import com.shocktrade.server.trading.{ContestDAO, OrderProcessor}
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.libs.Akka
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

/**
 * Contest I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class ContestActor extends Actor with ActorLogging {
  implicit val ec = Akka.system.dispatcher

  override def receive = {
    case action: ContestAgnosticAction => action.execute(sender())
    case message =>
      log.error(s"Unhandled message '$message' (${Option(message).map(_.getClass.getName).orNull})")
      unhandled(message)
  }
}

/**
 * Contest Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestActor {
  implicit val ec = Akka.system.dispatcher

  /**
   * Represents an asynchronous action
   * @author lawrence.daniels@gmail.com
   */
  sealed trait ContestAgnosticAction {

    def execute(mySender: ActorRef)(implicit ec: ExecutionContext)

  }

  /**
   * Represents an asynchronous action
   * @author lawrence.daniels@gmail.com
   */
  sealed trait ContestSpecificAction extends ContestAgnosticAction {

    def contestId: BSONObjectID

  }

  ///////////////////////////////////////////////////////////////////////////////
  //      Actor Messages
  ///////////////////////////////////////////////////////////////////////////////

  case class ApplyMarginInterest(contest: Contest) extends ContestSpecificAction {
    override def contestId = contest.id

    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.applyMarginInterest(contest) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach {
            _ foreach {
              WebSockets ! ContestUpdated(_)
            }
          }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class CreateContest(contest: Contest) extends ContestSpecificAction {
    override def contestId = contest.id

    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.createContest(contest) onComplete {
        case Success(lastError) =>
          WebSockets ! ContestCreated(contest)
          mySender ! lastError
        case Failure(e) => mySender ! e
      }
    }
  }

  case class CreateMarginAccount(contestId: BSONObjectID, playerId: BSONObjectID, account: MarginAccount) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.createMarginAccount(contestId, playerId, account) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          val outcome = for {
            c <- contest_?
            p <- c.participants.find(_.id == playerId)
          } yield (c, p)

          outcome.foreach { case (c, p) => WebSockets ! ParticipantUpdated(c, p) }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class CreateMessage(contestId: BSONObjectID, message: Message, fields: Seq[String]) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.createMessage(contestId, message) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! MessagesUpdated(_))
        case Failure(e) => mySender ! e
      }
    }
  }

  case class CloseOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String]) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.closeOrder(contestId, playerId, orderId) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! OrdersUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class CreateOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.createOrder(contestId, playerId, order) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! OrdersUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class DeleteContestByID(contestId: BSONObjectID) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.deleteContestByID(contestId) onComplete {
        case Success(lastError) =>
          mySender ! lastError
          WebSockets ! ContestDeleted(contestId)
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindContestByID(contestId: BSONObjectID, fields: Seq[String]) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.findContestByID(contestId, fields) onComplete {
        case Success(contest_?) => mySender ! contest_?
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindContests(searchOptions: SearchOptions, fields: Seq[String]) extends ContestAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      ContestDAO.findContests(searchOptions, fields) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindContestsByPlayerID(playerId: BSONObjectID, fields: Seq[String]) extends ContestAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.findContestsByPlayerID(playerId) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindContestsByPlayerName(playerName: String, fields: Seq[String]) extends ContestAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.findContestsByPlayerName(playerName) onComplete {
        case Success(contests) => mySender ! contests
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindOrderByID(contestId: BSONObjectID, orderId: BSONObjectID, fields: Seq[String]) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.findOrderByID(contestId, orderId) onComplete {
        case Success(contest_?) => mySender ! contest_?
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindAvailablePerks(contestId: BSONObjectID) extends ContestAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.findAvailablePerks(contestId) onComplete {
        case Success(perks) => mySender ! perks
        case Failure(e) => mySender ! e
      }
    }
  }

  case class JoinContest(contestId: BSONObjectID, participant: Participant) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.joinContest(contestId, participant) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }
    }
  }

  case class PurchasePerks(contestId: BSONObjectID, playerId: BSONObjectID, perkCodes: Seq[PerkType], totalCost: Double) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.purchasePerks(contestId, playerId, perkCodes, totalCost) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for (c <- contest_?; p <- c.participants.find(_.id == playerId)) {
            WebSockets ! PerksUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class ProcessOrders(contest: Contest, asOfDate: DateTime) extends ContestSpecificAction {
    override def contestId = contest.id

    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      val startTime = System.currentTimeMillis()

      // if trading was active during the as-of date
      Try(Await.result(OrderProcessor.processContest(contest, asOfDate), 5.seconds)) match {
        case Success(updateCount) =>
          val elapsedTime = System.currentTimeMillis() - startTime
          Logger.info(s"Finished processing orders in $elapsedTime msec(s)")

          // update the processing info
          ContestDAO.updateProcessingStats(contest.id, executionTime = elapsedTime, asOfDate = asOfDate)

          // if an update occurred, notify the users
          if (updateCount > 0) {
            ContestDAO.findContestByID(contest.id, fields = Nil) foreach {
              _ foreach (updatedContest => WebSockets ! ContestUpdated(updatedContest))
            }
          }

        case Failure(e) =>
          Logger.error(s"An error occur while processing contest '${contest.name}'", e)
      }
    }
  }

  case class QuitContest(contestId: BSONObjectID, playerId: BSONObjectID) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.quitContest(contestId, playerId) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }
    }
  }

  case class StartContest(contestId: BSONObjectID, startTime: Date) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.startContest(contestId, startTime) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          contest_?.foreach(WebSockets ! ContestUpdated(_))
        case Failure(e) => mySender ! e
      }
    }
  }

  case class TransferFundsBetweenAccounts(contestId: BSONObjectID, playerId: BSONObjectID, source: AccountType, amount: Double) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.transferFundsBetweenAccounts(contestId, playerId, source, amount) onComplete {
        case Success(contest_?) =>
          mySender ! contest_?
          for {
            c <- contest_?
            p <- c.participants.find(_.id == playerId)
          } {
            WebSockets ! ParticipantUpdated(c, p)
          }
        case Failure(e) => mySender ! e
      }
    }
  }

  case class UpdateProcessingHost(contestId: BSONObjectID, host: Option[String]) extends ContestSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) {
      ContestDAO.updateProcessingHost(contestId, host) onComplete {
        case Success(lastError) => mySender ! lastError
        case Failure(e) => mySender ! e
      }
    }
  }

}