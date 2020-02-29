package com.shocktrade.webapp.routes.account

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * Authentication DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait AuthenticationDAO {

  /**
   * Authenticates a user
   * @param username the given username
   * @param ec       the implicit [[ExecutionContext]]
   * @return the promise of an option of [[UserAccountData]]
   */
  def findByUsername(username: String)(implicit ec: ExecutionContext): Future[Option[UserAccountData]]

}

/**
 * Authentication DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object AuthenticationDAO {

  /**
   * Creates a new Authentication DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[AuthenticationDAO Authentication DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): AuthenticationDAO = new AuthenticationDAOMySQL(options)

}