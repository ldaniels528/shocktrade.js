package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Perks DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerkDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PerksDAO {

  /**
   * Retrieves the collection of available perks
   * @param ec the implicit [[ExecutionContext]]
   * @return the collection of available [[PerkData perks]]
   */
  override def findAvailablePerks(implicit ec: ExecutionContext): Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks WHERE enabled = 1") map { case (rows, _) => rows }
  }

  override def findPerks(implicit ec: ExecutionContext): Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks") map { case (rows, _) => rows }
  }

}
