package com.shocktrade.webapp.routes.account

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * User Account DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserAccountDAO {

  def createAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[String]]

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
