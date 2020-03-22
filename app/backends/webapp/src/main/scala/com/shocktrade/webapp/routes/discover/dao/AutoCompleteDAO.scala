package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.common.models.quote.AutoCompleteQuote
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Auto-Complete DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait AutoCompleteDAO {

  def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext): Future[js.Array[AutoCompleteQuote]]

}

/**
 * Auto-Complete DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object AutoCompleteDAO {

  /**
   * Creates a new Auto-Complete DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[AutoCompleteDAO Auto-Complete DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): AutoCompleteDAO = new AutoCompleteDAOMySQL(options)

}
