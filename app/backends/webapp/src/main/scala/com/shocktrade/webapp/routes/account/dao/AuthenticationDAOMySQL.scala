package com.shocktrade.webapp.routes.account.dao

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * Authentication DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AuthenticationDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with AuthenticationDAO {

  override def findByUsername(username: String)(implicit ec: ExecutionContext): Future[Option[UserAccountData]] = {
    conn.queryFuture[UserAccountData]("SELECT * FROM users WHERE username = ?", params = Seq(username))
      .map { case (rows, _) => rows.headOption }
  }

}
