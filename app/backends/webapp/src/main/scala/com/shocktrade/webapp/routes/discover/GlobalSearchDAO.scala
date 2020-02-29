package com.shocktrade.webapp.routes.discover

import com.shocktrade.common.models.EntitySearchResult
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Global Search DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait GlobalSearchDAO {

  def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext): Future[js.Array[EntitySearchResult]]

}

/**
 * Global Search DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object GlobalSearchDAO {

  /**
   * Creates a new Global Search DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[GlobalSearchDAO Search DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): GlobalSearchDAO = new GlobalSearchDAOMySQL(options)

}
