package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.ChartData
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Position DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PositionDAO {

  def findChart(contestID: String, userID: String, chart: String): Future[js.Array[ChartData]]

  def findPositions(contestID: String, userID: String): Future[js.Array[PositionData]]

}

/**
 * Position DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionDAO {

  /**
   * Creates a new Portfolio DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[PortfolioDAO Portfolio DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)(implicit ec: ExecutionContext): PositionDAO = new PositionDAOMySQL(options)

}
