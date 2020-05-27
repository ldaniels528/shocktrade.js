package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.forms._
import com.shocktrade.common.models.contest.{ChartData, PortfolioBalance}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with PortfolioDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  ///////////////////////////////////////////////////////////////////////
  //  Order Management
  ///////////////////////////////////////////////////////////////////////

  override def findOne(options: OrderSearchOptions): Future[Option[OrderData]] = orderSearch(options).map(_.headOption)

  override def orderSearch(options: OrderSearchOptions): Future[js.Array[OrderData]] = {
    val dict: List[(String, Any)] = {
        options.contestID.map(id => "P.contestID = ?" -> id).toList :::
        options.userID.map(id => "P.userID = ?" -> id).toList :::
        options.portfolioID.map(id => "P.portfolioID = ?" -> id).toList :::
        options.orderID.map(id => "O.orderID = ?" -> id).toList :::
        options.orderType.map(id => "O.orderType = ?" -> id).toList :::
        (options.status.toList flatMap {
          case OrderSearchOptions.ACTIVE_ORDERS => List("O.closed = ?" -> 0)
          case OrderSearchOptions.COMPLETED_ORDERS => List("O.closed = ?" -> 1)
          case OrderSearchOptions.FAILED_ORDERS => List("O.closed = ?" -> 1, "O.fulfilled = ?" -> 0)
          case OrderSearchOptions.FULFILLED_ORDERS => List("O.fulfilled = ?" -> 1)
          case _ => Nil
        })
    }
    val sql: String = {
      val sb = new StringBuilder(
        """|SELECT O.*, S.lastTrade
           |FROM orders O
           |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
           |LEFT JOIN mock_stocks S ON S.symbol = O.symbol
           |""".stripMargin
      )
      if (dict.nonEmpty) sb.append(" WHERE ")
      sb.append(dict.map(_._1).mkString(" AND "))
      sb.toString()
    }
    val values = dict.map(_._2)
    conn.queryFuture[OrderData](sql, values).map(_._1)
  }

  ///////////////////////////////////////////////////////////////////////
  //  Portfolio Management
  ///////////////////////////////////////////////////////////////////////

  override def findPortfolioBalance(contestID: String, userID: String): Future[Option[PortfolioBalance]] = {
    conn.queryFuture[PortfolioBalance](
      """|SELECT
         |    P.userID, P.funds,
         |    IFNULL(SUM(S.lastTrade * PS.quantity),0) equity,
         |    IFNULL(MAX(S.tradeDateTime), NOW()) AS asOfDate,
         |    (SELECT SUM(IFNULL(O.price, S.lastTrade) * O.quantity)
         |      FROM orders O
         |      INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |      LEFT JOIN mock_stocks S ON S.symbol = O.symbol
         |      WHERE O.orderType = 'BUY'
         |      AND O.closed = 0
         |      AND O.portfolioID = P.portfolioID
         |      AND P.contestID = ? AND P.userID = ?) AS totalBuyOrders,
         |    (SELECT SUM(S.lastTrade * O.quantity)
         |      FROM orders O
         |      INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |      LEFT JOIN mock_stocks S ON S.symbol = O.symbol
         |      WHERE O.orderType = 'SELL'
         |      AND O.closed = 0
         |      AND O.portfolioID = P.portfolioID
         |      AND P.contestID = ? AND P.userID = ?) AS totalSellOrders
         |FROM portfolios P
         |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT JOIN mock_stocks S ON S.symbol = PS.symbol
         |WHERE P.contestID = ? AND P.userID = ?
         |GROUP BY P.userID, P.funds
         |""".stripMargin,
      js.Array(contestID, userID, contestID, userID, contestID, userID)).map(_._1.headOption)
  }

  override def findPurchasedPerks(portfolioID: String): Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks WHERE portfolioID = ?", js.Array(portfolioID)).map(_._1)
  }

  override def findOne(options: PortfolioSearchOptions): Future[Option[PortfolioData]] = portfolioSearch(options).map(_.headOption)

  override def portfolioSearch(options: PortfolioSearchOptions): Future[js.Array[PortfolioData]] = {
    val dict: List[(String, Any)] = {
      options.contestID.map(id => "contestID = ?" -> id).toList :::
        options.userID.map(id => "userID = ?" -> id).toList :::
        options.portfolioID.map(id => "portfolioID = ?" -> id).toList
    }
    val sql: String = {
      val sb = new StringBuilder("SELECT * FROM portfolios")
      if (dict.nonEmpty) sb.append(" WHERE ")
      sb.append(dict.map(_._1).mkString(" AND "))
      sb.toString()
    }
    val values = dict.map(_._2)
    conn.queryFuture[PortfolioData](sql, values).map(_._1)
  }

  ///////////////////////////////////////////////////////////////////////
  //  Position Management
  ///////////////////////////////////////////////////////////////////////

  override def findChartData(contestID: String, userID: String, chart: String): Future[js.Array[ChartData]] = {
    chart match {
      case "contest" => findContestChart(contestID)
      case "exchange" => findExposureChartData(contestID, userID, column = "S.exchange")
      case "industry" => findExposureChartData(contestID, userID, column = "S.industry")
      case "sector" => findExposureChartData(contestID, userID, column = "S.sector")
      case "securities" => findExposureChartData(contestID, userID, column = "S.symbol")
      case unknown => Future.failed(throw js.JavaScriptException(s"Chart type '$unknown' is unrecognized"))
    }
  }

  private def findContestChart(contestID: String): Future[js.Array[ChartData]] = {
    conn.queryFuture[ChartData](
      s"""|SELECT U.username AS name, SUM(P.totalXP + 1) AS value
          |FROM portfolios P
          |LEFT JOIN users U ON U.userID = P.userID
          |WHERE P.contestID = ?
          |GROUP BY U.username
          |""".stripMargin, js.Array(contestID)).map(_._1)
  }

  private def findExposureChartData(contestID: String, userID: String, column: String): Future[js.Array[ChartData]] = {
    conn.queryFuture[ChartData](
      s"""|SELECT IFNULL($column, 'Unclassified') AS name, SUM(S.lastTrade * PS.quantity) AS value
          |FROM portfolios P
          |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
          |INNER JOIN mock_stocks S ON S.symbol = PS.symbol
          |WHERE P.contestID = ? AND P.userID = ?
          |AND PS.quantity > 0
          |GROUP BY $column
          |     UNION
          |SELECT 'Cash' AS name, P.funds AS value
          |FROM portfolios P
          |WHERE P.contestID = ? AND P.userID = ?
          |""".stripMargin, js.Array(contestID, userID, contestID, userID)).map(_._1)
  }

  override def findOne(options: PositionSearchOptions): Future[Option[PositionView]] = positionSearch(options).map(_.headOption)

  override def positionSearch(options: PositionSearchOptions): Future[js.Array[PositionView]] = {
    val dict: List[(String, Any)] = {
      options.contestID.map(id => "P.contestID = ?" -> id).toList :::
        options.userID.map(id => "P.userID = ?" -> id).toList :::
        options.portfolioID.map(id => "P.portfolioID = ?" -> id).toList :::
        options.positionID.map(id => "PS.positionID = ?" -> id).toList
    }
    val sql: String = {
      val sb = new StringBuilder(
        """|SELECT PS.*, S.lastTrade, S.tradeDateTime, S.name AS businessName, PS.quantity * S.lastTrade AS marketValue, S.high, S.low
           |FROM positions PS
           |INNER JOIN portfolios P on P.portfolioID = PS.portfolioID
           |LEFT JOIN mock_stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
           |WHERE PS.quantity > 0
           |""".stripMargin
      )
      if (dict.nonEmpty) sb.append(" AND ")
      sb.append(dict.map(_._1).mkString(" AND "))
      sb.toString()
    }
    val values = dict.map(_._2)
    conn.queryFuture[PositionView](sql, values).map(_._1)
  }

}
