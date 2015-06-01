package com.shocktrade.server.trading

import java.util.Date

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.ContestActor
import com.shocktrade.actors.ContestActor._
import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.PerkTypes.PerkType
import com.shocktrade.models.contest._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val finderActor = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 10)), name = "ContestFinder")
  private val contestActors = TrieMap[String, ActorRef]()

  private implicit val timeout: Timeout = 30.second
  private var lastEffectiveDate: DateTime = computeInitialFulfillmentDate
  private val frequency = 1 // TODO should be 5 (in minutes)

  /**
   * Starts the Order Processing System
   */
  def start() {
    Logger.info("Starting Order Processing System ...")

    // process orders once every 5 minutes
    system.scheduler.schedule(5.seconds, frequency.minutes)(processOrders())

    // process orders once per hour
    system.scheduler.schedule(1.hour, frequency.minutes)(applyMarginInterest())
    ()
  }

  def applyMarginInterest()(implicit timeout: Timeout) = {
    /*
    try {
      val currentTime = new Date()

      Logger.info(s"Applying interest charges for margin accounts...")
      ContestDAO.getActiveContests(lastEffectiveDate, lastEffectiveDate.minusMinutes(60)).enumerate().apply(Iteratee.foreach { contest =>
        contestActor(contest.id) ! ApplyMarginInterest(contest, lastEffectiveDate)
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }*/
  }

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? CloseOrder(contestId, playerId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (contestActor(contest.id) ? CreateContest(contest)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  def createMarginAccount(contestId: BSONObjectID, playerId: BSONObjectID, account: MarginAccount)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? CreateMarginAccount(contestId, playerId, account)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createMessage(contestId: BSONObjectID, message: Message)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? CreateMessage(contestId, message, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? CreateOrder(contestId, playerId, order)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def deleteContestByID(id: BSONObjectID)(implicit timeout: Timeout) = {
    (contestActor(id) ? DeleteContestByID(id)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  /**
   * Retrieves all of the system-defined perks
   * @return a promise of a sequence of perks
   */
  def findAvailablePerks(id: BSONObjectID) = ContestDAO.findAvailablePerks(id)

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor(id) ? FindContestByID(id, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def findContests(searchOptions: SearchOptions)(fields: String*)(implicit timeout: Timeout) = {
    (finderActor ? FindContests(searchOptions, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (finderActor ? FindContestsByPlayerID(playerId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findContestsByPlayerName(playerName: String)(fields: String*)(implicit timeout: Timeout) = {
    (finderActor ? FindContestsByPlayerName(playerName, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? FindOrderByID(contestId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def joinContest(id: BSONObjectID, participant: Participant)(implicit timeout: Timeout) = {
    (contestActor(id) ? JoinContest(id, participant)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  private def processOrders() {
    val currentTime = new Date()
    try {
      Logger.info(s"Processing order fulfillment [as of $lastEffectiveDate]...")
      ContestDAO.getActiveContests(lastEffectiveDate, lastEffectiveDate.minusMinutes(frequency)).enumerate().apply(Iteratee.foreach { contest =>
        contestActor(contest.id) ! ProcessOrders(contest, lastEffectiveDate)
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  /**
   * Purchases the passed perks
   * @param id the [[BSONObjectID contest ID]] which represents the contest
   * @param playerId the [[BSONObjectID player ID]] which represents the player whom is purchasing the perks
   * @param perkCodes the given perk codes
   * @param totalCost the total cost of the perks
   * @return a promise of an option of a contest
   */
  def purchasePerks(id: BSONObjectID, playerId: BSONObjectID, perkCodes: Seq[PerkType], totalCost: Double): Future[Option[Contest]] = {
    (contestActor(id) ? PurchasePerks(id, playerId, perkCodes, totalCost)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def quitContest(id: BSONObjectID, playerId: BSONObjectID)(implicit timeout: Timeout) = {
    (contestActor(id) ? QuitContest(id, playerId)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def startContest(id: BSONObjectID, startTime: Date)(implicit timeout: Timeout) = {
    (contestActor(id) ? StartContest(id, startTime)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def transferFundsBetweenAccounts(contestId: BSONObjectID, playerId: BSONObjectID, source: AccountType, amount: Double)(implicit timeout: Timeout) = {
    (contestActor(contestId) ? TransferFundsBetweenAccounts(contestId, playerId, source, amount)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def updateProcessingHost(contestId: BSONObjectID, host: Option[String])(implicit timeout: Timeout) = {
    (contestActor(contestId) ? UpdateProcessingHost(contestId, host)) map {
      case e: Exception => throw new IllegalStateException(e.getMessage, e)
      case response => response.asInstanceOf[LastError]
    }
  }

  /**
   * Ensures an actor instance per contest
   * @param id the given [[BSONObjectID contest ID]]
   * @return a reference to the actor that manages the contest
   */
  private def contestActor(id: BSONObjectID): ActorRef = {
    contestActors.getOrElseUpdate(id.stringify, system.actorOf(Props[ContestActor], name = s"ContestActor-${id.stringify}"))
  }

  private def computeInitialFulfillmentDate: Date = {
    val tradeStart = getTradeStartTime
    val currentTime = new Date()
    if (tradeStart >= currentTime) currentTime else tradeStart
  }

}