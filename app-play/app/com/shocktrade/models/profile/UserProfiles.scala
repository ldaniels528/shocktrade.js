package com.shocktrade.models.profile

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.UserProfileActor
import com.shocktrade.actors.UserProfileActor._
import com.shocktrade.controllers.ProfileController._
import play.libs.Akka
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * User Profiles Proxy
 * @author lawrence.daniels@gmail.com
 */
object UserProfiles {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private val finderActor = system.actorOf(Props[UserProfileActor].withRouter(RoundRobinPool(nrOfInstances = 20)), name = "ProfileFinder")
  private val profileActors = TrieMap[String, ActorRef]()
  private implicit val timeout: Timeout = 5.second
  private implicit val mc = db.collection[BSONCollection]("Players")

  /**
   * Creates the given user profile
   * @param profile the given user profile
   * @return a promise of the [[WriteResult outcome]]
   */
  def createProfile(profile: UserProfile): Future[WriteResult] = {
    (UserProfiles ? CreateProfile(profile)).mapTo[WriteResult]
  }

  /**
   * Retrieves a user profile by the user's name
   * @param userID the given user ID
   * @param amountToDeduct the amount to deduct
   * @return a promise of an option of a user profile
   */
  def deductFunds(userID: BSONObjectID, amountToDeduct: BigDecimal): Future[Option[UserProfile]] = {
    (UserProfiles ? DeductFunds(userID, amountToDeduct)).mapTo[Option[UserProfile]]
  }

  def findFacebookFriends(fbIds: Seq[String])(implicit ec: ExecutionContext) = {
    (finderActor ? FindFacebookFriends(fbIds)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[BS]]
    }
  }

  /**
   * Retrieves a user profile by the user's name
   * @param name the given user name (e.g. "ldaniels528")
   * @return a promise of an option of a user profile            
   */
  def findProfileByName(name: String): Future[Option[UserProfile]] = {
    (finderActor ? FindProfileByName(name)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[UserProfile]]
    }
  }

  /**
   * Retrieves a user profile by the user's Facebook ID
   * @param fbId the given user's Facebook ID
   * @return a promise of an option of a user profile 
   */
  def findProfileByFacebookID(fbId: String): Future[Option[UserProfile]] = {
    (finderActor ? FindProfileByFacebookID(fbId)) map {
      case e: Exception => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Option[UserProfile]]
    }
  }

  def !(action: ProfileAgnosticAction) = finderActor ! action

  def !(action: ProfileSpecificAction) = profileActor(action.userID) ! action

  def ?(action: ProfileAgnosticAction)(implicit timeout: Timeout) = (finderActor ? action) map {
    case e: Exception => throw new IllegalStateException(e.getMessage, e)
    case response => response
  }

  def ?(action: ProfileSpecificAction)(implicit timeout: Timeout) = (profileActor(action.userID) ? action) map {
    case e: Exception => throw new IllegalStateException(e.getMessage, e)
    case response => response
  }

  /**
   * Ensures an actor instance per contest
   * @param id the given [[BSONObjectID contest ID]]
   * @return a reference to the actor that manages the contest
   */
  private def profileActor(id: BSONObjectID): ActorRef = {
    profileActors.getOrElseUpdate(id.stringify, system.actorOf(Props[UserProfileActor], name = s"ProfileActor-${id.stringify}"))
  }

}
