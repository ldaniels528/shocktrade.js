package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Awards DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait AwardsDAO {

  /**
   * Retrieves the collection of available awards
   * @param ec the implicit [[ExecutionContext]]
   * @return the collection of available [[AwardData awards]]
   */
  def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]]

}

/**
 * Awards DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object AwardsDAO {

  /**
   * Creates a new Awards DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[AwardsDAO Awards DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): AwardsDAO = new AwardsDAOMySQL(options)

}