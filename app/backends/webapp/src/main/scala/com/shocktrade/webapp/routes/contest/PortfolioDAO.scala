package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PortfolioDAO {

  def computeTotalInvestment(portfolioID: String)(implicit ec: ExecutionContext): Future[Double] = ???

  def computeMarketValue(portfolioID: String)(implicit ec: ExecutionContext): Future[Double] = ???

  def create(portfolio: PortfolioData)(implicit ec: ExecutionContext): Future[Int]

  def findOneByUser(userID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]]

  def findOneByID(portfolioID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]]

  def findByContest(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[PortfolioData]]

  def findByUser(userID: String)(implicit ec: ExecutionContext): Future[js.Array[PortfolioData]]

  def findHeldSecurities(portfolioID: String)(implicit ec: ExecutionContext): Future[js.Array[String]]

  def findPurchasedPerks(portfolioID: String)(implicit ec: ExecutionContext): Future[js.Array[PerkData]] = ???

  def findParticipant(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]]

  def purchasePerks(portfolioID: String, purchasePerkCodes: Seq[String], perksCost: Double)(implicit ec: ExecutionContext): Future[Int] = ???

}

/**
 * Portfolio DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioDAO {

  /**
   * Creates a new Portfolio DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[PortfolioDAO Portfolio DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): PortfolioDAO = new PortfolioDAOMySQL(options)

}