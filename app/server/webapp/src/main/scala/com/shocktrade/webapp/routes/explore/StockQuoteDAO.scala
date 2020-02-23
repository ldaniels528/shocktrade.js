package com.shocktrade.webapp.routes.explore

import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Stock Quote DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait StockQuoteDAO {

  def findQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[ResearchQuote]]

  def findQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]]

}

/**
 * Stock Quote DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StockQuoteDAO {

  /**
   * Creates a new Explore DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[ExploreDAO Explore DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): StockQuoteDAO = new StockQuoteDAOMySQL(options)

}
