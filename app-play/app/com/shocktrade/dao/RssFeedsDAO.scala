package com.shocktrade.dao

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}

import scala.concurrent.ExecutionContext

/**
  * RSS Feeds DAO
  * @author lawrence.daniels@gmail.com
  */
case class RssFeedsDAO(reactiveMongoApi: ReactiveMongoApi) {
  private val mcR = reactiveMongoApi.db.collection[BSONCollection]("RssFeeds")

  def findFeed(id: String)(implicit ec: ExecutionContext) = {
    mcR.find(BS("_id" -> BSONObjectID(id))).one[BS]
  }

  /**
    * Retrieves the sources
    */
  def findSources(implicit ec: ExecutionContext) = {
    mcR.find(BS()).sort(BS("priority" -> 1)).cursor[BS]().collect[Seq]()
  }

}
