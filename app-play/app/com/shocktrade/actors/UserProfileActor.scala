package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.shocktrade.actors.UserProfileActor.ProfileAgnosticAction
import com.shocktrade.models.profile.{UserProfile, UserProfileDAO}
import play.libs.Akka
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * User Profile I/O Actor
 * @author lawrence.daniels@gmail.com
 */
class UserProfileActor extends Actor with ActorLogging {
  implicit val ec = Akka.system.dispatcher

  override def receive = {
    case action: ProfileAgnosticAction => action.execute(sender())
    case message =>
      log.error(s"Unhandled message '$message' (${Option(message).map(_.getClass.getName).orNull})")
      unhandled(message)
  }
}

/**
 * User Profile Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object UserProfileActor {
  implicit val ec = Akka.system.dispatcher

  /**
   * Represents an asynchronous action
   * @author lawrence.daniels@gmail.com
   */
  sealed trait ProfileAgnosticAction {

    def execute(mySender: ActorRef)(implicit ec: ExecutionContext)

  }

  /**
   * Represents an asynchronous action
   * @author lawrence.daniels@gmail.com
   */
  sealed trait ProfileSpecificAction extends ProfileAgnosticAction {

    def userID: BSONObjectID

  }

  ///////////////////////////////////////////////////////////////////////////////
  //      Actor Messages
  ///////////////////////////////////////////////////////////////////////////////

  case class CreateProfile(profile: UserProfile) extends ProfileAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      UserProfileDAO.createProfile(profile) onComplete {
        case Success(outcome) => mySender ! outcome
        case Failure(e) => mySender ! e
      }
    }
  }

  case class DeductFunds(userID: BSONObjectID, amountToDeduct: BigDecimal) extends ProfileSpecificAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      UserProfileDAO.deductFunds(userID, amountToDeduct) onComplete {
        case Success(profile_?) => mySender ! profile_?
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindProfileByFacebookID(fbId: String) extends ProfileAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      UserProfileDAO.findProfileByFacebookID(fbId) onComplete {
        case Success(profile_?) => mySender ! profile_?
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindFacebookFriends(fbIds: Seq[String]) extends ProfileAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      UserProfileDAO.findFacebookFriends(fbIds) onComplete {
        case Success(friends) => mySender ! friends
        case Failure(e) => mySender ! e
      }
    }
  }

  case class FindProfileByName(name: String) extends ProfileAgnosticAction {
    override def execute(mySender: ActorRef)(implicit ec: ExecutionContext) = {
      UserProfileDAO.findProfileByName(name) onComplete {
        case Success(profile_?) => mySender ! profile_?
        case Failure(e) => mySender ! e
      }
    }
  }

}
