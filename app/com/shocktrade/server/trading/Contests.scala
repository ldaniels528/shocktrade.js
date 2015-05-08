package com.shocktrade.server.trading

import java.util.Date

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.ContestActor
import com.shocktrade.actors.ContestActor._
import com.shocktrade.models.contest._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val contestActor = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 10)), name = "ContestUpdate")

  private implicit val timeout: Timeout = 30.second
  private var lastEffectiveDate: DateTime = computeInitialFulfillmentDate
  private val frequency = 5 // TODO should be 5 (in minutes)

  /**
   * Starts the Order Processing System
   */
  def start() {
    Logger.info("Starting Order Processing System ...")

    // process orders once every 5 minutes
    system.scheduler.schedule(5.seconds, frequency.minutes)(processOrders())
    ()
  }

  def processOrders() {
    try {
      val currentTime = new Date()

      Logger.info(s"Processing order fulfillment [as of $lastEffectiveDate]...")
      TradingDAO.getActiveContests(lastEffectiveDate, lastEffectiveDate.minusMinutes(frequency)).enumerate().apply(Iteratee.foreach { contest =>
        // first obtain an exclusive lock on the contest
        TradingDAO.lockContest(contest.id) onComplete {
          case Failure(e) =>
            Logger.error(s"Failed while attempting to lock Contest '${contest.name}'", e)
          case Success(contest_?) =>
            // if the lock was successfully retrieved, process the contest
            contest_? foreach { case (lockedContest, lockExpirationTime) =>
              contestActor ! ProcessOrders(lockedContest, lockExpirationTime, lastEffectiveDate)
            }
        }
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  private def computeInitialFulfillmentDate: Date = {
    val tradeStart = getTradeStartTime
    val currentTime = new Date()
    if (tradeStart >= currentTime) currentTime else tradeStart
  }

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? CloseOrder(contestId, playerId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (contestActor ? CreateContest(contest)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  def createMessage(contestId: BSONObjectID, message: Message)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? CreateMessage(contestId, message, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? CreateOrder(contestId, playerId, order, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def deleteContestByID(id: BSONObjectID)(implicit timeout: Timeout) = {
    (contestActor ? DeleteContestByID(id)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? FindContestByID(id, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def findContests(searchOptions: SearchOptions)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? FindContests(searchOptions, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? FindContestsByPlayerID(playerId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findContestsByPlayerName(playerName: String)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? FindContestsByPlayerName(playerName, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor ? FindOrderByID(contestId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def joinContest(id: BSONObjectID, participant: Participant)(implicit timeout: Timeout) = {
    (contestActor ? JoinContest(id, participant)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def quitContest(id: BSONObjectID, playerId: BSONObjectID)(implicit timeout: Timeout) = {
    (contestActor ? QuitContest(id, playerId)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def startContest(id: BSONObjectID, startTime: Date)(implicit timeout: Timeout) = {
    (contestActor ? StartContest(id, startTime)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

}