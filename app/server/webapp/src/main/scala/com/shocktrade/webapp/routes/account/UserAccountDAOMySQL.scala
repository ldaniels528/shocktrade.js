package com.shocktrade.webapp.routes.account

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User Account DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserAccountDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with UserAccountDAO {

  override def createAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[String]] = {
    import account._
    for {
      ok <- conn.executeFuture(
        """|INSERT INTO users (userID, username, email, password, wallet)
           |VALUES (uuid(), ?, ?, ?, ?)
           |""".stripMargin,
        js.Array(username, email, password, wallet)) if ok.affectedRows == 1

      newAccount <- conn.queryFuture[UserAccountData](
        "SELECT userID FROM users WHERE username = ?",
        js.Array(username)).map { case (rows, _) => rows.headOption }

    } yield newAccount.flatMap(_.userID.toOption)
  }

}
