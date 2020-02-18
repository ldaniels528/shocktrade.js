package com.shocktrade.server.dao.contest.mysql

import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.server.dao.contest.{PerkData, PerksDAO}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Perks DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerkDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PerksDAO {

  override def findAvailablePerks(implicit ec: ExecutionContext): Future[js.Array[PerkData]] = {
    conn.queryFuture[PerkData]("SELECT * FROM perks WHERE enabled = 1") map { case (rows, _) => rows }
  }

}
