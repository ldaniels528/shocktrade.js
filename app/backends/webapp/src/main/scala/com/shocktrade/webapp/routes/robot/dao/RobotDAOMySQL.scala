package com.shocktrade.webapp.routes.robot.dao

import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.SymbolData
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Robot DAO Implementation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with RobotDAO {

  override def findPendingOrderSymbols(username: String, portfolioID: String): Future[js.Array[String]] = {
    conn.queryFuture[SymbolData](
      """|SELECT O.symbol
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN orders O ON O.portfolioID = P.portfolioID
         |WHERE R.username = ? AND P.portfolioID = ?
         |""".stripMargin, js.Array(username, portfolioID)) map (_._1.flatMap(_.symbol.toOption))
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
