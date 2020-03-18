package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.models.ExposureData
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Position DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PositionDAO {

  def findExposure(contestID: String, userID: String, chart: String)(implicit ec: ExecutionContext): Future[js.Array[ExposureData]]

  def findPositions(portfolioID: String)(implicit ec: ExecutionContext): Future[js.Array[PositionData]]

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
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): PositionDAO = new PositionDAOMySQL(options)

}
