package com.shocktrade.webapp.routes.account

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User Account DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserAccountDAO {

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def createAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[UserAccountData]]

  def computeNetWorth(userID: String)(implicit ec: ExecutionContext): Future[Option[Double]]

  def deductFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int]

  def depositFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int]

  def updateNetWorth(userID: String, netWorth: Double): Future[Int]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  def findFavoriteSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[SymbolData]]

  def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Recent Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

  def findRecentSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[SymbolData]]

  def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int]

}

/**
 * User Account DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserAccountDAO {

  /**
   * Creates a new User Account DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[UserAccountDAO User Account DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): UserAccountDAO = new UserAccountDAOMySQL(options)

}
