package com.shocktrade.webapp.routes.account.dao

import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.UserDAOMySQL.UserAwardData
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User DAO (MySQL implementation)
 * @author lawrence.daniels@gmail.com
 */
class UserDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with UserDAO {
  private val userByIdSQL =
    """|SELECT U.*, SUM(P.funds) funds, IFNULL(SUM(S.lastTrade * PS.quantity),0) equity
       |FROM users U
       |LEFT JOIN portfolios P ON P.userID = U.userID
       |LEFT JOIN contests C ON C.contestID = P.contestID
       |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
       |LEFT JOIN stocks S ON S.symbol = PS.symbol
       |""".stripMargin

  override def findUserByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]] = {
    conn.queryFuture[UserProfileData](s"$userByIdSQL WHERE U.userID = ?", js.Array(id)) map { case (rows, _) => rows.headOption }
  }

  override def findUsersByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserProfileData]] = {
    conn.queryFuture[UserProfileData](s"$userByIdSQL WHERE U.userID IN (${ids.map(id => s"'$id'").mkString(",")})") map { case (rows, _) => rows }
  }

  override def findUserByName(name: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]] = {
    conn.queryFuture[UserProfileData](s"$userByIdSQL WHERE U.username = ?", js.Array(name)) map { case (rows, _) => rows.headOption }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def createUserAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Option[UserProfileData]] = {
    import account._
    for {
      ok <- conn.executeFuture(
        """|INSERT INTO users (userID, username, email, password, wallet)
           |VALUES (uuid(), ?, ?, ?, ?)
           |""".stripMargin,
        js.Array(username, email, password, wallet)) if ok.affectedRows == 1

      newAccount <- findUserByName(username.orNull)
    } yield newAccount
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Awards
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def findMyAwards(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
    conn.queryFuture[UserAwardData]("SELECT awardCode FROM user_awards WHERE userID = ?",
      js.Array(userID)).map(_._1.flatMap(_.awardCode.toOption))
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Icon Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def createIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Int] = {
    import icon._
    conn.executeFuture("REPLACE INTO user_icons (userID, name, mime, image) VALUES (?, ?, ?, ?)", js.Array(userID, name, mime, image))
      .map(_.affectedRows)
  }

  override def findUserIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]] = {
    conn.queryFuture[UserIconData]("SELECT * FROM user_icons WHERE userID = ?", js.Array(userID))
      .map { case (rows, _) => rows.headOption }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("REPLACE INTO favorite_symbols (userID, symbol) VALUES (?, ?)", js.Array(userID, symbol)) map (_.affectedRows)
  }

  override def findFavoriteSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[Ticker]] = {
    conn.queryFuture[Ticker]("SELECT symbol FROM favorite_symbols WHERE userID = ?", js.Array(userID)).map(_._1)
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

  override def findRecentSymbols(userID: String)(implicit ec: ExecutionContext): Future[js.Array[Ticker]] = {
    conn.queryFuture[Ticker]("SELECT symbol FROM recent_symbols WHERE userID = ?", js.Array(userID)).map(_._1)
  }

  override def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("DELETE FROM recent_symbols WHERE userID = ? AND symbol = ?", js.Array(userID, symbol)) map (_.affectedRows)
  }

}

/**
 * UserDAOMySQL Companion
 * @author lawrence.daniels@gmail.com
 */
object UserDAOMySQL {

  class UserAwardData(val awardCode: js.UndefOr[String]) extends js.Object

}