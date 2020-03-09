package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.NetWorth
import com.shocktrade.webapp.routes.SymbolData
import io.scalajs.npm.mysql.{MySQL, MySQLConnectionOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User DAO (MySQL implementation)
 * @author lawrence.daniels@gmail.com
 */
class UserDAOMySQL(options: MySQLConnectionOptions) extends UserDAO {
  private val conn = MySQL.createConnection(options)

  override def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name = '$id'") map { case (rows, _) =>
      rows.headOption
    }
  }

  override def findByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE id IN (${ids.map(id => s"'$id'").mkString(",")})") map { case (rows, _) => rows }
  }

  override def findByUsername(name: String)(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name like '$name%'")
      .map { case (rows, _) => rows }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

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

  override def computeNetWorth(userID: String)(implicit ec: ExecutionContext): Future[Option[NetWorth]] = {
    conn.queryFuture[NetWorth](
      """|SELECT U.userID, U.username, U.wallet, SUM(P.funds) funds, SUM(S.lastTrade * PS.quantity) equity
         |FROM users U
         |INNER JOIN contests C
         |INNER JOIN portfolios P ON P.contestID = C.contestID AND P.userID = U.userID
         |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
         |INNER JOIN stocks S ON S.symbol = PS.symbol
         |WHERE U.userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows.headOption }
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

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Icon Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def createIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Int] = {
    import icon._
    conn.executeFuture("INSERT INTO user_icons (userID, name, mime, image) VALUES (?, ?, ?, ?)", js.Array(userID, name, mime, image))
      .map(_.affectedRows)
  }

  override def findIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]] = {
    conn.queryFuture[UserIconData]("SELECT * FROM user_icons WHERE userID = ?", js.Array(userID))
      .map { case (rows, _) => rows.headOption }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("REPLACE INTO favorite_symbols (userID, symbol) VALUES (?, ?)", js.Array(userID, symbol)) map (_.affectedRows)
  }

  override def findFavoriteSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
    conn.queryFuture[SymbolData]("SELECT symbol FROM favorite_symbols WHERE userID = ?", js.Array(userID))
      .map { case (rows, _) => rows.flatMap(_.symbol.toOption) }
  }

  override def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("DELETE FROM favorite_symbols WHERE userID = ? AND symbol = ?", js.Array(userID, symbol)) map (_.affectedRows)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Recent Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("REPLACE INTO recent_symbols (userID, symbol) VALUES (?, ?)", js.Array(userID, symbol)) map (_.affectedRows)
  }

  override def findRecentSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
    conn.queryFuture[SymbolData]("SELECT symbol FROM recent_symbols WHERE userID = ?", js.Array(userID))
      .map { case (rows, _) => rows.flatMap(_.symbol.toOption) }
  }

  override def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("DELETE FROM recent_symbols WHERE userID = ? AND symbol = ?", js.Array(userID, symbol)) map (_.affectedRows)
  }

}

