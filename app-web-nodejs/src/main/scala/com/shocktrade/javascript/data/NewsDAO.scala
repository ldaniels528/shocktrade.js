package com.shocktrade.javascript.data

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * News Source DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsDAO extends Collection

/**
  * News Source DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object NewsDAO {

  /**
    * News Source DAO Extensions
    * @param newsDAO the given [[NewsDAO News DAO]]
    */
  implicit class NewsDAOExtensions(val newsDAO: NewsDAO) {

    /**
      * Retrieves a news source by ID
      * @param id the given news source ID
      * @return the promise of an option of a news source
      */
    @inline
    def findByID(id: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      newsDAO.findOneFuture[NewsSource]("_id" $eq id.$oid)
    }

    /**
      * Retrieves the news sources
      * @return the promise of a collection of a news sources
      */
    @inline
    def findSources(implicit ec: ExecutionContext) = {
      newsDAO.find().toArrayFuture[NewsSource]
    }
  }

  /**
    * News Source DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class NewsDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getNewsDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("RssFeeds").mapTo[NewsDAO]
    }
  }

}