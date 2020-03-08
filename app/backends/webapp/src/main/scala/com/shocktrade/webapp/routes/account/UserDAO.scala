package com.shocktrade.webapp.routes.account

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
   * @return a promise of the option of a users
   */
  def findByUsername(name: String)(implicit ec: ExecutionContext): Future[js.Array[UserData]]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Icon Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def createIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Int]

  def findIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def createAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[UserAccountData]]

  def computeNetWorth(userID: String)(implicit ec: ExecutionContext): Future[Option[Double]]

  def deductFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int]

  def depositFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  def findFavoriteSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]]

  def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Recent Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  def findRecentSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]]

  def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

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
