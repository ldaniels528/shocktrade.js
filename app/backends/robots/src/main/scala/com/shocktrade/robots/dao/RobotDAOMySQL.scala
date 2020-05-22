package com.shocktrade.robots.dao

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Robot DAO Implementation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with RobotDAO {

  override def findContestsToJoin(username: String, limit: Int): Future[js.Array[ContestRef]] = {
    conn.queryFuture[ContestRef](
      """|SELECT contestID, name
         |FROM contests
         |WHERE contestID NOT IN (
         |	SELECT C.contestID
         |	FROM users U
         |	INNER JOIN portfolios P ON U.userID = P.userID
         |	INNER JOIN contests C ON P.contestID = C.contestID
         |	INNER JOIN contest_statuses CS ON CS.statusID = C.statusID
         |	WHERE U.username = ?
         |  AND CS.status = 'ACTIVE'
         |  AND C.robotsAllowed = 1
         |)
         |AND robotsAllowed = 1
         |LIMIT ?
         |""".stripMargin, js.Array(username, limit)).map(_._1)
  }

  override def findPendingOrderTickers(username: String, portfolioID: String): Future[js.Array[Ticker]] = {
    conn.queryFuture[Ticker](
      """|SELECT O.symbol, O.exchange
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN orders O ON O.portfolioID = P.portfolioID
         |WHERE R.username = ? AND P.portfolioID = ?
         |""".stripMargin, js.Array(username, portfolioID)) map (_._1)
  }

  override def findOne(options: RobotSearchOptions): Future[Option[RobotPortfolioData]] = {
    search(options.copy(maxResults = 1)).map(_.headOption)
  }

  override def search(options: RobotSearchOptions): Future[js.Array[RobotPortfolioData]] = {
    val dict: List[(String, Any)] = {
      options.contestID.map(id => "C.contestID = ?" -> id).toList :::
        options.isActive.map(enabled => "R.isActive = ?" -> enabled).toList :::
        options.portfolioID.map(id => "P.portfolioID = ?" -> id).toList :::
        options.robotName.map(name => "R.username = ?" -> name).toList :::
        options.userID.map(id => "U.userID = ?" -> id).toList
    }
    val sql: String = {
      val sb = new StringBuilder(
        """|SELECT C.name AS contestName, U.userID, U.username, P.*, R.*
           |FROM robots R
           |INNER JOIN users U ON R.username = U.username
           |INNER JOIN portfolios P ON P.userID = U.userID
           |INNER JOIN contests C ON C.contestID = P.contestID
           |WHERE C.closedTime IS NULL
           |""".stripMargin
      )
      if (dict.nonEmpty) sb.append(" AND ")
      sb.append(dict.map(_._1).mkString(" AND "))
      options.maxResults.foreach(maxResults => s" LIMIT $maxResults")
      sb.toString()
    }
    val values = dict.map(_._2)
    conn.queryFuture[RobotPortfolioData](sql, values).map(_._1)
  }

  override def toggleRobot(username: String, isActive: Boolean): Future[Int] = {
    conn.executeFuture(
      """|UPDATE robots SET isActive = ? WHERE username = ?
         |""".stripMargin, js.Array(isActive, username)).map(_.affectedRows)
  }

}
