package com.shocktrade.webapp.routes.social

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Post DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PostDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PostDAO {

  override def findAll(limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]] = {
    conn.queryFuture[PostData](
      s"""|SELECT * FROM posts LIMIT ?
          |""".stripMargin,
      js.Array(limit)) map { case (rows, _) => rows }
  }

  override def findByUser(userID: String, limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]] = {
    conn.queryFuture[PostData](
      s"""|SELECT * FROM posts WHERE postID = ? LIMIT ?
          |""".stripMargin,
      js.Array(userID, limit)) map { case (rows, _) => rows }
  }

  override def findOneByID(postID: String)(implicit ec: ExecutionContext): Future[Option[PostData]] = {
    conn.queryFuture[PostData](
      s"""|SELECT * FROM posts WHERE postID = ?
          |""".stripMargin,
      js.Array(postID)) map { case (rows, _) => rows.headOption }
  }

  override def findNewsFeed(userID: String, limit: Int)(implicit ec: ExecutionContext): Future[js.Array[PostData]] = findByUser(userID, limit)

  override def findTags(postIDs: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[PostTagData]] = {
    conn.queryFuture[PostTagData](
      s"""|SELECT * FROM post_tags
          |WHERE postID IN ( ${postIDs.map(_ => "?").mkString(",")} )
          |""".stripMargin,
      js.Array(postIDs: _*)) map { case (rows, _) => rows }
  }

  override def like(postID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[Int]] = ???

  override def unlike(postID: String, userID: String)(implicit ec: ExecutionContext): Future[Option[Int]] = ???

  override def insertOne(post: PostData)(implicit ec: ExecutionContext): Future[Int] = ???

  override def deleteOne(postID: String)(implicit ec: ExecutionContext): Future[Int] = ???

  override def updateOne(post: PostData)(implicit ec: ExecutionContext): Future[Int] = ???

}
