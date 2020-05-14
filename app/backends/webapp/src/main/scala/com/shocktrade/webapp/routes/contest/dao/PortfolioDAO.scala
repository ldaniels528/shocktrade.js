package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.{ChartData, PortfolioBalance}
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

  def findOne(options: OrderSearchOptions): Future[Option[OrderData]]

  def search(options: OrderSearchOptions): Future[js.Array[OrderData]]

  ///////////////////////////////////////////////////////////////////////
  //  Portfolio Management
  ///////////////////////////////////////////////////////////////////////

  def findPortfolioBalance(contestID: String, userID: String): Future[Option[PortfolioBalance]]

  /**
   * Retrieves the collection of purchased perks
   * @return the collection of purchased [[PerkData perks]]
   */
  def findPurchasedPerks(portfolioID: String): Future[js.Array[PerkData]]

  def findOne(options: PortfolioSearchOptions): Future[Option[PortfolioData]]

  def search(options: PortfolioSearchOptions): Future[js.Array[PortfolioData]]

  ///////////////////////////////////////////////////////////////////////
  //  Position Management
  ///////////////////////////////////////////////////////////////////////

  def findChartData(contestID: String, userID: String, chart: String): Future[js.Array[ChartData]]

  def findOne(options: PositionSearchOptions): Future[Option[PositionView]]

  def search(options: PositionSearchOptions): Future[js.Array[PositionView]]

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