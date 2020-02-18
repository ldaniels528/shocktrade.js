package com.shocktrade.server.dao.reference

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * News Source DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait NewsSourceDAO {

  /**
   * Retrieves a news source by ID
   * @param id the given news source ID
   * @return the promise of an option of a news source
   */
  def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[NewsSourceData]]

  /**
   * Retrieves the news sources
   * @return the promise of a collection of a news sources
   */
  def findSources(implicit ec: ExecutionContext): Future[js.Array[NewsSourceData]]

}

/**
  * News Source DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewsSourceDAO {

  /**
   * Creates a new News Source DAO instance
   * @param options the given [[MySQLConnectionOptions connection options]]
   * @return a new [[NewsSourceDAO News Source DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): NewsSourceDAO = new NewsSourceDAOMySQL(options)

}