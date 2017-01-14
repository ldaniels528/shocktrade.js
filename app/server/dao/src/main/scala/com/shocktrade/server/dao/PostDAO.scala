package com.shocktrade.server.dao

import io.scalajs.npm.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Post DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PostDAO extends Collection

/**
  * Post DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object PostDAO {

  /**
    * Post DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class PostDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getPostDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Posts").mapTo[PostDAO]
    }

  }

}

