package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.{ChartData, PortfolioBalance}
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PortfolioDAO {

  ///////////////////////////////////////////////////////////////////////
  //  Order Management
  ///////////////////////////////////////////////////////////////////////

  def cancelOrder(orderID: String): Future[Int]

  def createOrder(portfolioID: String, order: OrderData): Future[Int]

  def createOrder(contestID: String, userID: String, order: OrderData): Future[Int]

  def findOrders(contestID: String, userID: String): Future[js.Array[OrderData]]

  ///////////////////////////////////////////////////////////////////////
  //  Perks Management
  ///////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the collection of available perks
   * @return the collection of available [[PerkData perks]]
   */
  def findAvailablePerks: Future[js.Array[PerkData]]

  /**
   * Retrieves the collection of perks
   * @return the collection of available [[PerkData perks]]
   */
  def findPerks: Future[js.Array[PerkData]]

  def findPurchasedPerks(portfolioID: String): Future[js.Array[PerkData]] = ???

  def purchasePerks(portfolioID: String, purchasePerkCodes: Seq[String], perksCost: Double): Future[Int] = ???

  ///////////////////////////////////////////////////////////////////////
  //  Portfolio Management
  ///////////////////////////////////////////////////////////////////////

  def findPortfolioBalance(contestID: String, userID: String): Future[Option[PortfolioBalance]]

  def findPortfolioByID(portfolioID: String): Future[Option[PortfolioData]]

  def findPortfolioByUser(contestID: String, userID: String): Future[Option[PortfolioData]]

  def findPortfoliosByContest(contestID: String): Future[js.Array[PortfolioData]]

  def findPortfoliosByUser(userID: String): Future[js.Array[PortfolioData]]

  ///////////////////////////////////////////////////////////////////////
  //  Position Management
  ///////////////////////////////////////////////////////////////////////

  def findChartData(contestID: String, userID: String, chart: String): Future[js.Array[ChartData]]

  def findHeldSecurities(portfolioID: String): Future[js.Array[Ticker]]

  def findPositions(contestID: String, userID: String): Future[js.Array[PositionData]]

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
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)(implicit ec: ExecutionContext): PortfolioDAO = {
    new PortfolioDAOMySQL(options)
  }

}