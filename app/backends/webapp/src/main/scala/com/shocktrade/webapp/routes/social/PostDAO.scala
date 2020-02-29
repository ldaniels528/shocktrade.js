package com.shocktrade.webapp.routes.social

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Post DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PostDAO {

  def findAll(limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]]

  def findByUser(userID: String, limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]]

  def findOneByID(postID: String)(implicit ec: ExecutionContext): Future[Option[PostData]]

  def findNewsFeed(userID: String, limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]]

  def findTags(postIDs: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[PostTagData]]

  def like(postID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[Int]]

  def unlike(postID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[Int]]

  def insertOne(post: PostData)(implicit ec: ExecutionContext): Future[Int]

  def deleteOne(postID: String)(implicit ec: ExecutionContext): Future[Int]

  def updateOne(post: PostData)(implicit ec: ExecutionContext): Future[Int]

}

/**
 * Post DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PostDAO {

  /**
   * Creates a new Post DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[PostDAO Post DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): PostDAO = new PostDAOMySQL(options)

}
