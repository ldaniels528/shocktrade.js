package com.shocktrade.robots.dao

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Robot DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RobotDAO {

  def findContestsToJoin(robotUsername: String, limit: Int): Future[js.Array[ContestRef]]

  def findPendingOrderTickers(robotUsername: String, portfolioID: String): Future[js.Array[Ticker]]

  def findRobot(username: String): Future[js.Array[RobotPortfolioData]]

  def findRobotPortfolios: Future[js.Array[RobotPortfolioData]]

  def findRobots(isActive: Boolean): Future[js.Array[RobotPortfolioData]]

  def setRobotActivity(username: String, activity: String): Future[Int]

  def toggleRobot(username: String, isActive: Boolean): Future[Int]

}

/**
 * Robot DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotDAO {

  /**
   * Creates a new Stock Market Data Generator instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[RobotDAO robot DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)(implicit ec: ExecutionContext): RobotDAO = new RobotDAOMySQL(options)

}
