package com.shocktrade.webapp.routes.account.dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserDAO {

  /**
   * Attempts to retrieve a user by ID
   * @param id the given user ID
   * @return a promise of an option of a user
   */
  def findUserByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]]

  /**
   * Attempts to retrieve a user by ID
   * @param ids the given collection of user IDs
   * @return a promise of an option of a user
   */
  def findUsersByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserProfileData]]

  /**
   * Attempts to retrieve a user by username
   * @param name the given username
   * @return a promise of the option of a users
   */
  def findUserByName(name: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Icon Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def findUserIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]]

  def findUsernameIcon(username: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Awards
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def findMyAwards(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]]

}

/**
 * User DAO Companion
 * @author lawrence.daniels@gmail.com
 */
object UserDAO {

  /**
   * Creates a new User DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[UserDAO User DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): UserDAO = new UserDAOMySQL(options)

}
