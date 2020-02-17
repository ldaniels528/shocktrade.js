package com.shocktrade.server.dao.contest

import io.scalajs.npm.mysql.{ConnectionOptions, MySQL}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the Awards DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class AwardsDAOMySQL(options: ConnectionOptions) extends AwardsDAO {
  private val conn = MySQL.createConnection(options)

  override def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]] = {
    conn.queryFuture[AwardData](s"SELECT * FROM awards") map { case (rows, _) => rows }
  }

}
