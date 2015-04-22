package com.shocktrade.models.contest

import java.util.Date

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.ContestActor
import com.shocktrade.actors.ContestActor._
import play.api.libs.json._
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
  private val reader = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 50)))
  private val writer = system.actorOf(Props[ContestActor])

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID, order: JsObject)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CloseOrder(contestId, playerId, orderId, order, fields)).mapTo[Option[JsValue]]
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (writer ? CreateContest(contest)).mapTo[LastError]
  }

  def createOrder(contestId: BSONObjectID, playerName: String, order: Order)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CreateOrder(contestId, playerName, order, fields)).mapTo[Option[JsValue]]
  }

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestByID(id, fields)).mapTo[Option[JsObject]]
  }

  def findContests(searchOptions: SearchOptions)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContests(searchOptions, fields)).mapTo[Seq[JsValue]]
  }

  def findContestsByPlayerName(playerName: String)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestsByPlayerName(playerName, fields)).mapTo[Seq[JsValue]]
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestsByPlayerID(playerId, fields)).mapTo[Seq[JsValue]]
  }

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindOrderByID(contestId, orderId, fields)).mapTo[Option[JsObject]]
  }

  def findOrders(id: BSONObjectID, playerName: String)(implicit timeout: Timeout) = {
    (reader ? FindOrders(id, playerName)).mapTo[Seq[JsValue]]
  }

  def sendMessage(contestId: BSONObjectID, sentBy: String, text: String, sentTime: Date = new Date())(implicit timeout: Timeout) = {
    (writer ? SendMessage(contestId, sentBy, text, sentTime)).mapTo[Option[JsObject]]
  }

}