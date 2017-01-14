package com.shocktrade.server.dao

import io.scalajs.npm.mongodb.Db
import io.scalajs.npm.mongodb.gridfs.{GridFSBucket, GridFSOptions}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Post Attachment DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PostAttachmentDAO extends GridFSBucket

/**
  * Post Attachment DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object PostAttachmentDAO {

  /**
    * Post Attachment DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class PostAttachmentDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getPostAttachmentDAO(implicit ec: ExecutionContext): PostAttachmentDAO = {
      new GridFSBucket(db, new GridFSOptions(bucketName = "PostAttachments")).asInstanceOf[PostAttachmentDAO]
    }

  }

}
