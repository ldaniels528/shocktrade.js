package com.shocktrade.webapp.routes.robot.dao

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
         |  INNER JOIN contest_statuses CS ON CS.statusID = C.statusID
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

  override def findRobot(username: String): Future[js.Array[RobotData]] = {
    conn.queryFuture[RobotData](
      """|SELECT C.name AS contestName, U.userID, P.contestID, P.portfolioID, P.funds, R.*
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN contests C ON C.contestID = P.contestID
         |WHERE R.username = ?
         |""".stripMargin, js.Array(username)) map (_._1)
  }

  override def findRobots: Future[js.Array[RobotData]] = {
    conn.queryFuture[RobotData](
      """|SELECT C.name AS contestName, U.userID, P.contestID, P.portfolioID, P.funds, R.*
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN contests C ON C.contestID = P.contestID
         |""".stripMargin) map (_._1)
  }

  override def findRobots(isActive: Boolean): Future[js.Array[RobotData]] = {
    conn.queryFuture[RobotData](
      """|SELECT C.name AS contestName, U.userID, P.contestID, P.portfolioID, P.funds, R.*
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN contests C ON C.contestID = P.contestID
         |WHERE R.isActive = ?
         |""".stripMargin, js.Array(isActive)) map (_._1)
  }

  override def setRobotActivity(username: String, activity: String): Future[Int] = {
    conn.executeFuture(
      """|UPDATE robots
         |SET lastActivity = ?, lastActiveTime = now()
         |WHERE username = ?
         |""".stripMargin, js.Array(activity, username)).map(_.affectedRows)
  }

  override def toggleRobot(username: String, isActive: Boolean): Future[Int] = {
    conn.executeFuture(
      """|UPDATE robots
         |SET isActive = ?
         |WHERE username = ?
         |""".stripMargin, js.Array(isActive, username)).map(_.affectedRows)
  }

}
