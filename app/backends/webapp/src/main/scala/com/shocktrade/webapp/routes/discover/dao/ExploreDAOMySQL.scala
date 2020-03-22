package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.common.models.quote.{CompleteQuote, ExploreQuote, ResearchQuote}
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Explore DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ExploreDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ExploreDAO {

  override def exploreIndustries(sector: String)(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]] = ???

  override def exploreSectors(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]] = ???

  override def exploreSubIndustries(sector: String, industry: String)(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]] = ???

  override def findCompleteQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[CompleteQuote]] = ???

  override def findQuotesByIndustry(sector: String, industry: String)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]] = ???

  override def findQuotesBySubIndustry(sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]] = ???

}
