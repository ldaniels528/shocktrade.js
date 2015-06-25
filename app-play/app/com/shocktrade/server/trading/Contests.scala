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
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.{postfixOps, implicitConversions}

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val finderActor = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 20)), name = "ContestFinder")
  private val contestActors = TrieMap[String, ActorRef]()
  private implicit val timeout: Timeout = 10.second

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (Contests ? CloseOrder(contestId, playerId, orderId, fields)).mapTo[Option[Contest]]
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (Contests ? CreateContest(contest)).mapTo[LastError]
  }

  def createMarginAccount(contestId: BSONObjectID, playerId: BSONObjectID, account: MarginAccount)(implicit timeout: Timeout) = {
    (Contests ? CreateMarginAccount(contestId, playerId, account)).mapTo[Option[Contest]]
  }

  def createMessage(contestId: BSONObjectID, message: Message)(fields: String*)(implicit timeout: Timeout) = {
    (Contests ? CreateMessage(contestId, message, fields)).mapTo[Option[Contest]]
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(implicit timeout: Timeout) = {
    (Contests ? CreateOrder(contestId, playerId, order)).mapTo[Option[Contest]]
  }

  def deleteContestByID(id: BSONObjectID)(implicit timeout: Timeout) = {
    (Contests ? DeleteContestByID(id)).mapTo[LastError]
  }

  /**
   * Retrieves all of the system-defined perks
   * @return a promise of a sequence of perks
   */
  def findAvailablePerks(id: BSONObjectID)(implicit timeout: Timeout) = {
    (Contests ? FindAvailablePerks(id)).mapTo[Seq[Perk]]
  }

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (Contests ? FindContestByID(id, fields)).mapTo[Option[Contest]]
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
    (Contests ? FindOrderByID(contestId, orderId, fields)).mapTo[Option[Contest]]
  }

  def joinContest(id: BSONObjectID, participant: Participant)(implicit timeout: Timeout) = {
    (Contests ? JoinContest(id, participant)).mapTo[Option[Contest]]
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
    (Contests ? PurchasePerks(id, playerId, perkCodes, totalCost)).mapTo[Option[Contest]]
  }

  def quitContest(id: BSONObjectID, playerId: BSONObjectID)(implicit timeout: Timeout) = {
    (Contests ? QuitContest(id, playerId)).mapTo[Option[Contest]]
  }

  def startContest(id: BSONObjectID, startTime: Date)(implicit timeout: Timeout) = {
    (Contests ? StartContest(id, startTime)).mapTo[Option[Contest]]
  }

  def transferFundsBetweenAccounts(contestId: BSONObjectID, playerId: BSONObjectID, source: AccountType, amount: Double)(implicit timeout: Timeout) = {
    (Contests ? TransferFundsBetweenAccounts(contestId, playerId, source, amount)).mapTo[Option[Contest]]
  }

  def updateProcessingHost(contestId: BSONObjectID, host: Option[String])(implicit timeout: Timeout) = {
    (Contests ? UpdateProcessingHost(contestId, host)).mapTo[LastError]
  }

  def !(action: ContestAgnosticAction) = finderActor ! action

  def !(action: ContestSpecificAction) = contestActor(action.contestId) ! action

  def ?(action: ContestAgnosticAction)(implicit timeout: Timeout) = (finderActor ? action) map {
    case e: Throwable => throw new IllegalStateException(e.getMessage, e)
    case response => response
  }

  def ?(action: ContestSpecificAction)(implicit timeout: Timeout) = (contestActor(action.contestId) ? action) map {
    case e: Throwable => throw new IllegalStateException(e.getMessage, e)
    case response => response
  }

  /**
   * Ensures an actor instance per contest
   * @param id the given [[BSONObjectID contest ID]]
   * @return a reference to the actor that manages the contest
   */
  private def contestActor(id: BSONObjectID): ActorRef = {
    contestActors.getOrElseUpdate(id.stringify, system.actorOf(Props[ContestActor], name = s"ContestActor-${id.stringify}"))
  }

}