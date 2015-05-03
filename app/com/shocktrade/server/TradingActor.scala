package com.shocktrade.server

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.shocktrade.actors.WebSockets
import com.shocktrade.actors.WebSockets.ContestUpdated
import com.shocktrade.models.contest._
import com.shocktrade.server.TradingActor._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Trading Actor
 * @author lawrence.daniels@gmail.com
 */
class TradingActor() extends Actor with ActorLogging {
  private implicit val timeout: Timeout = 30.second

  import context.dispatcher

  override def receive = {
    case ProcessOrders(contest, lockExpirationTime, asOfDate) =>
      processOrders(contest, lockExpirationTime, asOfDate)
    case message =>
      unhandled(message)
  }

  /**
   * Process all active orders
   * @param contest the given [[Contest contest]]
   * @param asOfDate the given effective date
   */
  private def processOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime) {
    // if trading was active during the as-of date
    TradingProcessor.processContest(contest, asOfDate) onComplete {
      case Success(updateCount) =>
        // if an update occurred, notify the users
        if (updateCount > 0) {
          Contests.findContestByID(contest.id)() foreach {
            _ foreach { updatedContest =>
              WebSockets ! ContestUpdated(updatedContest)
            }
          }
        }

        // finally unlock the contest
        unlock(contest, lockExpirationTime)

      case Failure(e) =>
        log.error(s"An error occur while processing contest '${contest.name}'", e)

        // finally unlock the contest
        unlock(contest, lockExpirationTime)
    }
  }

  /**
   * Unlocks the contest
   * @param contest the given [[Contest contest]]
   * @param lockExpirationTime the given lock expiration [[DateTime date]]
   */
  private def unlock(contest: Contest, lockExpirationTime: DateTime): Unit = {
    TradingDAO.unlockContest(contest.id, lockExpirationTime) onComplete {
      case Failure(e) =>
        log.error(e, s"Failed while attempting to unlock Contest '${contest.name}'")
      case Success(_) =>
    }
  }

}

/**
 * Trading Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object TradingActor {

  case class ProcessOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime)

}
