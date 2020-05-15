package com.shocktrade.webapp.routes.account.dao

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
  //    Awards
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def findMyAwards(userID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
    conn.queryFuture[UserAwardData]("SELECT awardCode FROM user_awards WHERE userID = ?",
      js.Array(userID)).map(_._1.flatMap(_.awardCode.toOption))
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Icon Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def findUserIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]] = {
    conn.queryFuture[UserIconData]("SELECT * FROM user_icons WHERE userID = ?", js.Array(userID))
      .map { case (rows, _) => rows.headOption }
  }

  override def findUsernameIcon(username: String)(implicit ec: ExecutionContext): Future[Option[UserIconData]] = {
    conn.queryFuture[UserIconData](
      """|SELECT UI.* FROM user_icons UI
         |INNER JOIN users U ON U.userID = UI.userID
         |WHERE U.username = ?
         |""".stripMargin, js.Array(username))
      .map { case (rows, _) => rows.headOption }
  }

}

/**
 * UserDAOMySQL Companion
 * @author lawrence.daniels@gmail.com
 */
object UserDAOMySQL {

  class UserAwardData(val awardCode: js.UndefOr[String]) extends js.Object

}