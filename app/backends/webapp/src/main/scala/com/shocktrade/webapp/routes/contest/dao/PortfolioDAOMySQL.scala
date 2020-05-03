package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.{ChartData, PortfolioBalance}
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with PortfolioDAO {

  ///////////////////////////////////////////////////////////////////////
  //  Order Management
  ///////////////////////////////////////////////////////////////////////

  override def findOrderByID(orderID: String): Future[Option[OrderData]] = {
    conn.queryFuture[OrderData](
      """|SELECT O.*, S.lastTrade
         |FROM orders O
         |LEFT  JOIN stocks S ON S.symbol = O.symbol
         |WHERE O.orderID = ?
         |""".stripMargin,
      js.Array(orderID)).map(_._1.headOption)
  }

  override def findOrders(contestID: String, userID: String): Future[js.Array[OrderData]] = {
    conn.queryFuture[OrderData](
      """|SELECT O.*, S.lastTrade
         |FROM orders O
         |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |LEFT  JOIN stocks S ON S.symbol = O.symbol
         |WHERE P.contestID = ? AND P.userID = ?
         |AND (O.closed = 0 OR now() < DATE_ADD(O.processedTime, INTERVAL 1 DAY))
         |""".stripMargin,
      js.Array(contestID, userID)).map(_._1)
  }

  ///////////////////////////////////////////////////////////////////////
  //  Perk Management
  ///////////////////////////////////////////////////////////////////////

  override def findPurchasedPerks(portfolioID: String): Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks WHERE portfolioID = ?", js.Array(portfolioID)).map(_._1)
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
         |    (SELECT SUM(S.lastTrade * O.quantity)
         |      FROM orders O
         |      INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |      LEFT JOIN stocks S ON S.symbol = O.symbol
         |      WHERE O.orderType = 'BUY'
         |      AND O.closed = 0
         |      AND O.portfolioID = P.portfolioID
         |      AND P.userID = ?) AS totalBuyOrders,
         |    (SELECT SUM(S.lastTrade * O.quantity)
         |      FROM orders O
         |      INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |      LEFT JOIN stocks S ON S.symbol = O.symbol
         |      WHERE O.orderType = 'SELL'
         |      AND O.closed = 0
         |      AND O.portfolioID = P.portfolioID
         |      AND P.userID = ?) AS totalSellOrders
         |FROM portfolios P
         |INNER JOIN contests C ON C.contestID = P.contestID
         |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT JOIN stocks S ON S.symbol = PS.symbol
         |WHERE C.contestID = ? AND P.userID = ?
         |GROUP BY P.userID, P.funds
         |""".stripMargin,
      js.Array(userID, userID, contestID, userID)).map(_._1.headOption)
  }

  override def findPortfolioByID(portfolioID: String): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE portfolioID = ?
         |""".stripMargin,
      js.Array(portfolioID)).map(_._1.headOption)
  }

  override def findPortfolioByUser(contestID: String, userID: String): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT * FROM portfolios WHERE contestID = ? AND userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)).map(_._1.headOption)
  }

  override def findPortfolioIdByUser(contestID: String, userID: String): Future[Option[String]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT portfolioID FROM portfolios WHERE contestID = ? AND userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows.headOption.flatMap(_.portfolioID.toOption) }
  }

  override def findPortfoliosByContest(contestID: String): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE contestID = ?
         |""".stripMargin,
      js.Array(contestID)).map(_._1)
  }

  override def findPortfoliosByUser(userID: String): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)).map(_._1)
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
      s"""|SELECT U.username AS name, SUM(P.funds) + SUM(IFNULL(s.lastTrade,0) * IFNULL(PS.quantity,0)) AS value
          |FROM contests C
          |LEFT JOIN portfolios P ON P.contestID = C.contestID
          |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
          |LEFT JOIN stocks S ON S.symbol = PS.symbol
          |LEFT JOIN users U ON U.userID = P.userID
          |WHERE C.contestID = ?
          |GROUP BY U.username
          |""".stripMargin, js.Array(contestID)).map(_._1)
  }

  private def findExposureChartData(contestID: String, userID: String, column: String): Future[js.Array[ChartData]] = {
    conn.queryFuture[ChartData](
      s"""|SELECT IFNULL($column, 'Unclassified') AS name, SUM(S.lastTrade * PS.quantity) AS value
          |FROM users U
          |INNER JOIN portfolios P ON P.userID = P.userID
          |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
          |INNER JOIN stocks S ON S.symbol = PS.symbol
          |INNER JOIN contests C ON C.contestID = P.contestID
          |WHERE C.contestID = ? AND U.userID = ? AND PS.quantity > 0
          |GROUP BY $column
          |     UNION
          |SELECT 'Cash' AS name, P.funds AS value
          |FROM users U
          |INNER JOIN portfolios P ON P.userID = U.userID
          |INNER JOIN contests C ON C.contestID = P.contestID
          |WHERE C.contestID = ? AND U.userID = ?
          |""".stripMargin, js.Array(contestID, userID, contestID, userID)).map(_._1)
  }

  override def findHeldSecurities(portfolioID: String): Future[js.Array[Ticker]] = {
    conn.queryFuture[Ticker](
      """|SELECT S.symbol, S.exchange, S.lastTrade, S.tradeDateTime,
         |       S.name AS businessName,
         |       S.lastTrade * PS.quantity AS marketValue
         |FROM portfolios P
         |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE P.portfolioID = ?
         |AND PS.quantity > 0
         |""".stripMargin,
      js.Array(portfolioID)).map(_._1)
  }

  override def findPositionByID(positionID: String): Future[Option[PositionData]] = {
    conn.queryFuture[PositionData](
      """|SELECT PS.*, S.lastTrade, S.name AS businessName, S.lastTrade * PS.quantity AS marketValue
         |FROM positions PS
         |LEFT JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE PS.positionID = ?
         |""".stripMargin,
      js.Array(positionID)).map(_._1.headOption)
  }

  override def findPositions(contestID: String, userID: String): Future[js.Array[PositionData]] = {
    conn.queryFuture[PositionData](
      """|SELECT PS.*, S.lastTrade, S.name AS businessName, PS.quantity * S.lastTrade AS marketValue
         |FROM positions PS
         |INNER JOIN portfolios P on P.portfolioID = PS.portfolioID AND PS.quantity > 0
         |LEFT  JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE P.contestID = ? AND P.userID = ?
         |""".stripMargin, js.Array(contestID, userID)).map(_._1)
  }

}
