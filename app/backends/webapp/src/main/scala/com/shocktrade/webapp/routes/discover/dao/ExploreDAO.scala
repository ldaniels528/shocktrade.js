package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.common.models.quote.{CompleteQuote, ExploreQuote, ResearchQuote}
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Explore DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ExploreDAO {

  def exploreIndustries(sector: String)(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]]

  def exploreSectors(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]]

  def exploreSubIndustries(sector: String, industry: String)(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]]

  def findCompleteQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[CompleteQuote]]

  def findQuotesByIndustry(sector: String, industry: String)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]]

  def findQuotesBySubIndustry(sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]]

}

/**
 * Explore DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ExploreDAO {

  /**
   * Creates a new Explore DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[ExploreDAO Explore DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): ExploreDAO = new ExploreDAOMySQL(options)

}
