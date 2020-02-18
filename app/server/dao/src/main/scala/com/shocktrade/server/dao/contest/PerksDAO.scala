package com.shocktrade.server.dao.contest

import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.server.dao.contest.mysql.PerkDAOMySQL
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Perks DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PerksDAO {

  /**
   * Retrieves the collection of available perks
   * @param ec  the implicit [[ExecutionContext]]
   * @return the collection of available [[PerkData perks]]
   */
  def findAvailablePerks(implicit ec: ExecutionContext): Future[js.Array[PerkData]]

}

/**
 * Perks DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PerksDAO {

  /**
   * Creates a new Perks DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[PerksDAO Perks DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): PerksDAO = new PerkDAOMySQL(options)

}