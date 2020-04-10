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

  override def cancelOrder(orderID: String): Future[Int] = {
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = 1, processedTime = now(), statusMessage = ?
         |WHERE orderID = ?
         |AND close = 0
         |""".stripMargin,
      js.Array("Canceled", orderID)) map (_.affectedRows)
  }

  override def createOrder(portfolioID: String, order: OrderData): Future[Int] = {
    import order._
    conn.executeFuture(
      """|INSERT INTO orders (orderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)
         |VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(portfolioID, symbol, exchange, orderType, priceType, price, quantity)) map (_.affectedRows)
  }

  override def createOrder(contestID: String, userID: String, order: OrderData): Future[Int] = {
    import order._
    conn.executeFuture(
      """|INSERT INTO orders (orderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)
         |SELECT uuid(), portfolioID, ?, ?, ?, ?, ?, ?
         |FROM portfolios
         |WHERE contestID = ? AND userID = ?
         |""".stripMargin,
      js.Array(symbol, exchange, orderType, priceType, price, quantity, contestID, userID)) map (_.affectedRows)
  }

  override def findOrders(contestID: String, userID: String): Future[js.Array[OrderData]] = {
    conn.queryFuture[OrderData](
      """|SELECT O.*, S.lastTrade
         |FROM orders O
         |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |LEFT  JOIN stocks S ON S.symbol = O.symbol
         |WHERE P.contestID = ? AND P.userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows }
  }

  ///////////////////////////////////////////////////////////////////////
  //  Perk Management
  ///////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the collection of available perks
   * @return the collection of available [[PerkData perks]]
   */
  override def findAvailablePerks: Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks WHERE enabled = 1") map { case (rows, _) => rows }
  }

  override def findPerks: Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks") map { case (rows, _) => rows }
  }

  ///////////////////////////////////////////////////////////////////////
  //  Portfolio Management
  ///////////////////////////////////////////////////////////////////////

  override def findPortfolioBalance(contestID: String, userID: String): Future[Option[PortfolioBalance]] = {
    conn.queryFuture[PortfolioBalance](
      """|SELECT
         |  C.contestID, C.name, U.userID, U.username, U.wallet, P.funds, IFNULL(MAX(SP.tradeDateTime), NOW()) AS asOfDate, SUM(SP.lastTrade * PS.quantity) equity,
         |  SUM(CASE WHEN O.orderType = 'BUY' AND O.fulfilled = 0 THEN SO.lastTrade * O.quantity ELSE 0 END) AS totalBuyOrders,
         |  SUM(CASE WHEN O.orderType = 'SELL' AND O.fulfilled = 0 THEN SO.lastTrade * O.quantity ELSE 0 END) AS totalSellOrders
         |FROM users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN contests C ON C.contestID = P.contestID
         |LEFT JOIN orders O ON O.portfolioID = P.portfolioID
         |LEFT JOIN stocks SO ON SO.symbol = O.symbol
         |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT JOIN stocks SP ON SP.symbol = PS.symbol
         |WHERE C.contestID = ? AND U.userID = ? AND PS.active = 1
         |GROUP BY C.contestID, C.name, U.userID, U.username, U.wallet, P.funds
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows.headOption }
  }

  override def findPortfolioByID(portfolioID: String): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE portfolioID = ?
         |""".stripMargin,
      js.Array(portfolioID)) map { case (rows, _) => rows.headOption }
  }

  override def findPortfolioByUser(contestID: String, userID: String): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT * FROM portfolios WHERE contestID = ? AND userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows.headOption }
  }

  override def findPortfoliosByContest(contestID: String): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE contestID = ?
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

  override def findPortfoliosByUser(userID: String): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows }
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
      s"""|SELECT U.username AS name, P.funds + SUM(IFNULL(s.lastTrade,0) * IFNULL(PS.quantity,0)) AS value
          |FROM contests C
          |LEFT JOIN portfolios P ON P.contestID = C.contestID
          |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
          |LEFT JOIN stocks S ON S.symbol = PS.symbol
          |LEFT JOIN users U ON U.userID = P.userID
          |WHERE C.contestID = ? AND PS.active = 1
          |GROUP BY U.username, P.funds
          |""".stripMargin, js.Array(contestID)).map { case (rows, _) => rows }
  }

  private def findExposureChartData(contestID: String, userID: String, column: String): Future[js.Array[ChartData]] = {
    conn.queryFuture[ChartData](
      s"""|SELECT IFNULL($column, 'Unclassified') AS name, SUM(S.lastTrade * PS.quantity) AS value
          |FROM users U
          |INNER JOIN portfolios P ON P.userID = P.userID
          |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
          |INNER JOIN stocks S ON S.symbol = PS.symbol
          |INNER JOIN contests C ON C.contestID = P.contestID
          |WHERE C.contestID = ? AND U.userID = ?
          |GROUP BY $column
          |     UNION
          |SELECT 'Cash' AS name, P.funds AS value
          |FROM users U
          |INNER JOIN portfolios P ON P.userID = U.userID
          |INNER JOIN contests C ON C.contestID = P.contestID
          |WHERE C.contestID = ? AND U.userID = ?
          |AND PS.active = 1
          |""".stripMargin, js.Array(contestID, userID, contestID, userID)).map { case (rows, _) => rows }
  }

  override def findHeldSecurities(portfolioID: String): Future[js.Array[Ticker]] = {
    conn.queryFuture[Ticker](
      """|SELECT S.symbol, S.exchange, S.lastTrade, S.tradeDateTime
         |FROM portfolios P
         |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
         |INNER JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE P.portfolioID = ?
         |AND PS.active = 1
         |""".stripMargin,
      js.Array(portfolioID)) map { case (rows, _) => rows }
  }

  override def findPositions(contestID: String, userID: String): Future[js.Array[PositionData]] = {
    conn.queryFuture[PositionData](
      """|SELECT PS.*, S.lastTrade, (S.lastTrade - PS.price)/PS.price AS gainLossPct
         |FROM positions PS
         |INNER JOIN portfolios P ON P.portfolioID = PS.portfolioID
         |LEFT  JOIN stocks S ON S.symbol = PS.symbol
         |WHERE P.contestID = ?
         |AND P.userID = ?
         |AND PS.active = 1
         |""".stripMargin, js.Array(contestID, userID)) map { case (rows, _) => rows }
  }

}
