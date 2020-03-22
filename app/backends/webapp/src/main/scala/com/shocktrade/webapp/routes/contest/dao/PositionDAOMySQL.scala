package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.ExposureData
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Position DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PositionDAO {

  override def findChart(contestID: String, userID: String, chart: String)(implicit ec: ExecutionContext): Future[js.Array[ExposureData]] = {
    val column = chart match {
      case "exchange" => "S.exchange"
      case "industry" => "S.industry"
      case "sector" => "S.sector"
      case "securities" => "S.symbol"
      case unknown => Future.failed(js.JavaScriptException(s"Chart type '$unknown' is unrecognized"))
    }
    conn.queryFuture[ExposureData](
      s"""|SELECT IFNULL($column, 'Unclassified') AS name, SUM(S.lastTrade * PS.quantity) AS value
          |FROM users U
          |INNER JOIN portfolios P ON P.userID = P.userID
          |INNER JOIN contests C ON C.contestID = P.contestID
          |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
          |INNER JOIN stocks S ON S.symbol = PS.symbol
          |WHERE C.contestID = ? AND U.userID = ?
          |GROUP BY $column
          |""".stripMargin, js.Array(contestID, userID)).map { case (rows, _) => rows }
  }

  override def findPositions(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[js.Array[PositionData]] = {
    conn.queryFuture[PositionData](
      """|SELECT PS.*
         |FROM positions PS
         |INNER JOIN portfolios P ON P.portfolioID = PS.portfolioID
         |WHERE P.contestID = ? AND P.userID = ?
         |""".stripMargin, js.Array(contestID, userID)) map { case (rows, _) => rows }
  }

}

