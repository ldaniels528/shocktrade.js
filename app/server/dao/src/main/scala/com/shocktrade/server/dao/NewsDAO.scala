package com.shocktrade.server.dao

import io.scalajs.npm.mongodb._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * News Source DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NewsDAO extends Collection

/**
  * News Source DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewsDAO {

  /**
    * News Source DAO Extensions
    * @param dao the given [[NewsDAO News DAO]]
    */
  implicit class NewsDAOExtensions(val dao: NewsDAO) {

    /**
      * Retrieves a news source by ID
      * @param id the given news source ID
      * @return the promise of an option of a news source
      */
    @inline
    def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[NewsSourceData]] = {
      dao.findOneFuture[NewsSourceData]("_id" $eq id.$oid)
    }

    /**
      * Retrieves the news sources
      * @return the promise of a collection of a news sources
      */
    @inline
    def findSources(implicit ec: ExecutionContext): Future[js.Array[NewsSourceData]] = {
      dao.find[NewsSourceData]().toArray()
    }
  }

  /**
    * News Source DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class NewsDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getNewsDAO: NewsDAO = {
      db.collection("RssFeeds").asInstanceOf[NewsDAO]
    }
  }

}