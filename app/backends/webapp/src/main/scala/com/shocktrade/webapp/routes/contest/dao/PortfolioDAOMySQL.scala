package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.PortfolioBalance
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PortfolioDAO {

  override def create(portfolio: PortfolioData)(implicit ec: ExecutionContext): Future[Int] = {
    import portfolio._
    conn.executeFuture(
      """|INSERT INTO portfolios (portfolioID, contestID, userID, funds, asOfDate, active)
         |VALUES (uuid(), ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, userID, funds, asOfDate, active)) map (_.affectedRows)
  }

  override def findOneByUser(userID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows.headOption }
  }

  override def findOneByID(portfolioID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE portfolioID = ?
         |""".stripMargin,
      js.Array(portfolioID)) map { case (rows, _) => rows.headOption }
  }

  override def findByContest(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE contestID = ?
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

  override def findPortfolio(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT * FROM portfolios WHERE contestID = ? AND userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows.headOption }
  }

  override def findPortfolioBalance(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioBalance]] = {
    conn.queryFuture[PortfolioBalance](
      """|SELECT
         |	C.contestID, C.name, U.userID, U.username, U.wallet, P.funds, SUM(SP.lastTrade * PS.quantity) equity,
         |    SUM(CASE WHEN O.orderType = 'BUY' AND O.fulfilled = 0 THEN SO.lastTrade * O.quantity ELSE 0 END) AS totalBuyOrders,
         |    SUM(CASE WHEN O.orderType = 'SELL' AND O.fulfilled = 0 THEN SO.lastTrade * O.quantity ELSE 0 END) AS totalSellOrders
         |FROM users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |INNER JOIN contests C ON C.contestID = P.contestID
         |LEFT JOIN orders O ON O.portfolioID = P.portfolioID
         |LEFT JOIN stocks SO ON SO.symbol = O.symbol
         |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT JOIN stocks SP ON SP.symbol = PS.symbol
         |WHERE C.contestID = ? AND U.userID = ?
         |GROUP BY C.contestID, C.name, U.userID, U.username, U.wallet, P.funds
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows.headOption }
  }

  override def findByUser(userID: String)(implicit ec: ExecutionContext): Future[js.Array[PortfolioData]] = {
    conn.queryFuture[PortfolioData](
      """|SELECT *
         |FROM portfolios
         |WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows }
  }

  override def findHeldSecurities(portfolioID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
    conn.queryFuture[Ticker](
      """|SELECT S.symbol
         |FROM portfolios P
         |INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
         |INNER JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE P.portfolioID = ?
         |""".stripMargin,
      js.Array(portfolioID)) map { case (rows, _) => rows.flatMap(_.symbol.toOption) }
  }

}
