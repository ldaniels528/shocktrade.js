package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.controllers.ContestResources._
import com.shocktrade.models.contest.Contest
import play.libs.Akka
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future
import scala.reflect.ClassTag

/**
 * Contest Management Proxy
 * @author lawrence.daniels@gmail.com
 */
object Contests {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val reader = system.actorOf(Props[ContestActor].withRouter(RoundRobinPool(nrOfInstances = 50)))
  private val writer = system.actorOf(Props[ContestActor])
  private val mcC = db.collection[JSONCollection]("Contests")

  /**
   * Allows a user/process to "tell" the actor some fact or process a command
   * @param message the given command or fact
   */
  def !(message: AnyRef): Unit = {
    message match {
      case msg: Writable => writer ! msg
      case msg => reader ! msg
    }
  }

  /**
   * Allows a user/process to "ask" the actor a question
   * @param message the given question
   * @param timeout the timeout duration
   * @tparam T the response's return type
   * @return the response message
   */
  def ?[T: ClassTag](message: AnyRef)(implicit timeout: Timeout): Future[T] = {
    import akka.pattern.ask

    (reader ? message).mapTo[T]
  }

  /**
   * Contest Actor
   * @author lawrence.daniels@gmail.com
   */
  class ContestActor extends Actor with ActorLogging {
    override def receive = {
      case CreateContest(contest) =>
        createContest(contest)

      case message =>
        log.info(s"Unhandled message: $message (${message.getClass.getName})")
        unhandled(message)
    }

    private def createContest(c: Contest): Unit = {
      mcC.insert(c.toJson)
    }

  }

  trait Writable

  case class CreateContest(contest: Contest) extends Writable

}