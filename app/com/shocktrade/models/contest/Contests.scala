package com.shocktrade.models.contest

import java.util.Date

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.ContestReaderActor._
import com.shocktrade.actors.ContestUpdateActor._
import com.shocktrade.actors.{ContestReaderActor, ContestUpdateActor}
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
  private val reader = system.actorOf(Props[ContestReaderActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "ContestReader")
  private val writer = system.actorOf(Props[ContestUpdateActor], name = "ContestUpdate")

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CloseOrder(contestId, playerId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createContest(contest: Contest)(implicit timeout: Timeout) = {
    (writer ? CreateContest(contest)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  def createMessage(contestId: BSONObjectID, message: Message)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CreateMessage(contestId, message, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(fields: String*)(implicit timeout: Timeout) = {
    (writer ? CreateOrder(contestId, playerId, order, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def deleteContestByID(id: BSONObjectID)(implicit timeout: Timeout) = {
    (writer ? DeleteContestByID(id)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[LastError]
    }
  }

  def findContestByID(id: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestByID(id, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def findContests(searchOptions: SearchOptions)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContests(searchOptions, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindContestsByPlayerID(playerId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[Contest]]
    }
  }

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(fields: String*)(implicit timeout: Timeout) = {
    (reader ? FindOrderByID(contestId, orderId, fields)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def joinContest(id: BSONObjectID, participant: Participant)(implicit timeout: Timeout) = {
    (writer ? JoinContest(id, participant)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def quitContest(id: BSONObjectID, playerId: BSONObjectID)(implicit timeout: Timeout) = {
    (writer ? QuitContest(id, playerId)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

  def startContest(id: BSONObjectID, startTime: Date)(implicit timeout: Timeout) = {
    (writer ? StartContest(id, startTime)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[Contest]]
    }
  }

}