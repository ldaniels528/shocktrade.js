package com.shocktrade.client.profile

import com.shocktrade.common.models.user.User
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.{Factory, injected}
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * User Factory
  * @author lawrence.daniels@gmail.com
  */
class UserFactory(@injected("UserService") userService: UserService) extends Factory {
  private val cache = js.Dictionary[Future[User]]()

  ///////////////////////////////////////////////////////////////////////////
  //      CRUD Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Asynchronously retrieves a user instance for the given user ID
    * @param userId the given user ID
    * @param ec     the given [[ExecutionContext execution context]]
    * @return a promise of the user instance
    */
  def getUserByID(userId: String)(implicit ec: ExecutionContext): Future[User] = {
    cache.getOrElseUpdate(userId, {
      console.log(s"Loading user information for # $userId...")
      val promise = userService.getUserByID(userId)
      promise onComplete {
        case Success(_) =>
        case Failure(e) =>
          console.log(s"Unexpected failure: ${e.displayMessage}")
          cache.delete(userId)
      }
      promise
    })
  }

  /**
    * Retrieves a set of users by ID
    * @param userIds the given user IDs
    * @param ec      the given [[ExecutionContext execution context]]
    * @return the array of [[User users]]
    */
  def getUsers(userIds: js.Array[String])(implicit ec: ExecutionContext): Future[js.Array[User]] = {
    val missingUserIds = userIds.filterNot(cache.contains)
    for {
      missingUsers <- if (missingUserIds.nonEmpty) userService.getUsers(missingUserIds).toFuture else Future.successful(emptyArray[User])
      _ = missingUsers.foreach(u => cache.put(u._id.orNull, Future.successful(u)))
      users <- Future.sequence(userIds.toSeq map getUserByID) map (seq => js.Array(seq: _*))
    } yield users
  }

}
