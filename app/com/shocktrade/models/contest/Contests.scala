package com.shocktrade.models.contest

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.ContestActor
import com.shocktrade.actors.ContestActor._
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val reader = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "ContestReader")
  private val writer = system.actorOf(Props[ContestActor], name = "ContestWriter")

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CloseOrder(contestId, playerId, orderId, fields)).mapTo[Option[Contest]]
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (writer ? CreateContest(contest)).mapTo[LastError]
  }

  def createMessage(contestId: BSONObjectID, message: Message)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CreateMessage(contestId, message, fields)).mapTo[Option[Contest]]
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CreateOrder(contestId, playerId, order, fields)).mapTo[Option[Contest]]
  }

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestByID(id, fields)).mapTo[Option[Contest]]
  }

  def findContests(searchOptions: SearchOptions)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContests(searchOptions, fields)).mapTo[Seq[Contest]]
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestsByPlayerID(playerId, fields)).map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindOrderByID(contestId, orderId, fields)).mapTo[Option[Order]]
  }

  def findOrders(id: BSONObjectID, playerId: BSONObjectID)(implicit timeout: Timeout) = {
    (reader ? FindOrders(id, playerId)).mapTo[Seq[Order]]
  }

}