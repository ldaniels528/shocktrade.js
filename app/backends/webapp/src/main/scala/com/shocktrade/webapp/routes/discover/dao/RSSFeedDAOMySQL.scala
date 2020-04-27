package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * RSS Feed DAO (MySQL Version)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RSSFeedDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with RSSFeedDAO {

  /**
   * Retrieves a RSS feed by ID
   * @param id the given RSS feed ID
   * @return the promise of an option of a RSS feed
   */
  def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[RSSFeedData]] = {
    conn.queryFuture[RSSFeedData]("SELECT * FROM rss_feeds WHERE rssFeedID = ?", js.Array(id)).map(_._1.headOption)
  }

  /**
   * Retrieves the RSS feeds
   * @return the promise of a collection of a RSS feeds
   */
  def findRSSFeeds(implicit ec: ExecutionContext): Future[js.Array[RSSFeedData]] = {
    conn.queryFuture[RSSFeedData]("SELECT * FROM rss_feeds").map(_._1)
  }

}
