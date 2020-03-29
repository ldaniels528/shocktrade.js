package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.{AwardData, AwardsDAO}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Awards DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with AwardsDAO {

  override def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]] = {
    conn.queryFuture[AwardData](s"SELECT * FROM awards") map(_._1)
  }

}
