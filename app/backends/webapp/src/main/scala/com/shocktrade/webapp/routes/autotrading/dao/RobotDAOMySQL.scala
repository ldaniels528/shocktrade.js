package com.shocktrade.webapp.routes.autotrading.dao

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

 override def findPendingOrderSymbols(robotUsername: String, portfolioID: String): Future[js.Array[String]] = {
    conn.queryFuture[SymbolData](
      """|SELECT O.symbol
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN orders O ON O.portfolioID = P.portfolioID
         |WHERE R.username = ? AND P.portfolioID = ?
         |""".stripMargin, js.Array(robotUsername, portfolioID)) map (_._1.flatMap(_.symbol.toOption))
  }

  override def findRobots: Future[js.Array[RobotData]] = {
    conn.queryFuture[RobotData](
      """|SELECT U.userID, U.username, P.contestID, P.portfolioID, P.funds, R.strategy
         |FROM robots R
         |INNER JOIN users U ON R.username = U.username
         |INNER JOIN portfolios P ON P.userID = U.userID
         |""".stripMargin) map (_._1)
  }

}
