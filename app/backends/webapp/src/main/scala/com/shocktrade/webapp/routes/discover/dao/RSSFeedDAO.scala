package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * RSS Feed DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RSSFeedDAO {

  /**
   * Retrieves a news source by ID
   * @param id the given news source ID
   * @return the promise of an option of a news source
   */
  def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[RSSFeedData]]

  /**
   * Retrieves the news sources
   * @return the promise of a collection of a news sources
   */
  def findRSSFeeds(implicit ec: ExecutionContext): Future[js.Array[RSSFeedData]]

}

/**
 * RSS Feed DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RSSFeedDAO {

  /**
   * Creates a new RSS Feed DAO instance
   * @param options the given [[MySQLConnectionOptions connection options]]
   * @return a new [[RSSFeedDAO RSS Feed DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): RSSFeedDAO = new RSSFeedDAOMySQL(options)

}