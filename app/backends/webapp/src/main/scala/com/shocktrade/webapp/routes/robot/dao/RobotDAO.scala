package com.shocktrade.webapp.routes.robot
package dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Robot DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RobotDAO {

  def findPendingOrderSymbols(robotUsername: String, portfolioID: String): Future[js.Array[String]]

  def findRobot(username: String): Future[js.Array[RobotData]]

  def findRobots: Future[js.Array[RobotData]]

  def findRobots(isActive: Boolean): Future[js.Array[RobotData]]

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
