package com.shocktrade.webapp.routes.autotrading
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

  def findRobots(implicit ec: ExecutionContext): Future[js.Array[RobotData]]

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
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): RobotDAO = new RobotDAOImpl()

  /**
   * Robot DAO Implementation
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class RobotDAOImpl() extends RobotDAO {
    private val robots = js.Array[RobotData](new RobotData(name = "daisy"))

    override def findRobots(implicit ec: ExecutionContext): Future[js.Array[RobotData]] = Future.successful(robots)

  }

}
