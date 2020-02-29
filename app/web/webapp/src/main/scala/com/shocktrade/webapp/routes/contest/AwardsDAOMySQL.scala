package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the Awards DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class AwardsDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with AwardsDAO {

  override def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]] = {
    conn.queryFuture[AwardData](s"SELECT * FROM awards") map { case (rows, _) => rows }
  }

}
