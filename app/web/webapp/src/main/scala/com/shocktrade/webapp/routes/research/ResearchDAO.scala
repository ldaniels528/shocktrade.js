package com.shocktrade.webapp.routes.research

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Research DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ResearchDAO {

  def research(options: ResearchOptions)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]]

}

/**
 * Research DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResearchDAO {

  /**
   * Creates a new Research DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[ResearchDAO Research DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): ResearchDAO = new ResearchDAOMySQL(options)

}