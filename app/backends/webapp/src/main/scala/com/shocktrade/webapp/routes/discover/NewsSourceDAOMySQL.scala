package com.shocktrade.webapp.routes.discover

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * News Source DAO (MySQL Version)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsSourceDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with NewsSourceDAO {

  /**
   * Retrieves a news source by ID
   * @param id the given news source ID
   * @return the promise of an option of a news source
   */
  def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[NewsSourceData]] = {
    conn.queryFuture[NewsSourceData]("SELECT * FROM newsSources WHERE _id = ?", params = Seq(id)) map { case (rows, _) => rows.headOption }
  }

  /**
   * Retrieves the news sources
   * @return the promise of a collection of a news sources
   */
  def findSources(implicit ec: ExecutionContext): Future[js.Array[NewsSourceData]] = {
    conn.queryFuture[NewsSourceData]("SELECT * FROM newsSources") map { case (rows, _) => rows }
  }

}
