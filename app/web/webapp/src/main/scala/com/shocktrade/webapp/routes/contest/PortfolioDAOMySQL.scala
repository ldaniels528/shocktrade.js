package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.contest.PortfolioDAOMySQL.Ticker
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

/**
 * PortfolioDAOMySQL Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioDAOMySQL {

  class Ticker(val symbol: js.UndefOr[String]) extends js.Object

}
