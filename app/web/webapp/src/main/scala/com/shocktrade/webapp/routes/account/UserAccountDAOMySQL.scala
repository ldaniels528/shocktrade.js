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

  override def createAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[UserAccountData]] = {
    import account._
    for {
      ok <- conn.executeFuture(
        """|INSERT INTO users (userID, username, email, password, wallet)
           |VALUES (uuid(), ?, ?, ?, ?)
           |""".stripMargin,
        js.Array(username, email, password, wallet)) if ok.affectedRows == 1

      newAccount <- conn.queryFuture[UserAccountData](
        "SELECT * FROM users WHERE username = ?",
        js.Array(username)).map { case (rows, _) => rows.headOption }

    } yield newAccount
  }

  override def computeNetWorth(userID: String)(implicit ec: ExecutionContext): Future[Option[Double]] = {
    conn.queryFuture[UserAccountData](
      """|SELECT wallet
         |FROM portfolios
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows.headOption.flatMap(_.wallet.toOption) }
  }

  override def deductFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture(
      """|UPDATE users
         |SET wallet = wallet - ?
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(amount, userID)) map (_.affectedRows)
  }

  override def depositFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture(
      """|UPDATE users
         |SET wallet = wallet + ?
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(amount, userID)) map (_.affectedRows)
  }

  override def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = ???

  override def findFavoriteSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[SymbolData]] = ???

  override def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = ???

  override def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = ???

  override def findRecentSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[SymbolData]] = ???

  override def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = ???

  override def updateNetWorth(userID: String, netWorth: Double): Future[Int] = ???

}
