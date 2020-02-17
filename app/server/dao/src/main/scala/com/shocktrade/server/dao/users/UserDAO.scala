package com.shocktrade.server.dao.users

import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.server.dao.events._
import com.shocktrade.server.dao.users.events.UserEvent
import io.scalajs.npm.mysql.ConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * User DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait UserDAO extends EventConsumer[UserEvent] {

  /**
    * Attempts to consume the given event
    * @param event the given [[SourcedEvent event]]
    * @return the option of a future representing the consumption of the event
    */
  override def consume(event: UserEvent): Option[Future[Boolean]] = None

  /**
    * Attempts to retrieve a user by ID
    * @param id the given user ID
    * @return a promise of an option of a user
    */
  def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserData]]

  /**
    * Attempts to retrieve a user by ID
    * @param ids the given collection of user IDs
    * @return a promise of an option of a user
    */
  def findByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserData]]

  /**
    * Attempts to retrieve a user by username
    * @param name the given username
    * @return a promise of a collection of users
    */
  def findByUsername(name: String)(implicit ec: ExecutionContext): Future[js.Array[UserData]]

}

/**
  * User DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object UserDAO {

  /**
    * Creates a new User DAO instance
    * @param options the given [[ConnectionOptions]]
    * @return a new [[UserDAO User DAO]]
    */
  def apply(options: ConnectionOptions = DataAccessObjectHelper.getConnectionOptions): UserDAO = new UserDAOMySQL(options)

}
