package com.shocktrade.dao

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}

import scala.concurrent.ExecutionContext

/**
  * Online Status DAO
  * @author lawrence.daniels@gmail.com
  */
case class OnlineStatusDAO(reactiveMongoApi: ReactiveMongoApi) {
  private val mcO = reactiveMongoApi.db.collection[BSONCollection]("OnlineStatuses")

  def findGroupStatus(userIDs: Seq[String])(implicit ec: ExecutionContext) = {
    mcO.find(BS("_id" -> BS("$in" -> userIDs))).cursor[BS]().collect[Seq]()
  }

  def findStatus(userID: String)(implicit ec: ExecutionContext) = {
    mcO.find(BS("_id" -> userID.toBSID)).one[BS]
  }

  def setConnectedStatus(userID: String, newState: Boolean)(implicit ec: ExecutionContext) = {
    mcO.findAndUpdate(
      selector = BS("_id" -> userID.toBSID),
      update = BS("$set" -> BS("connected" -> newState, "updatedTime" -> new Date)),
      fetchNewObject = false, upsert = true
    ) map(_.result)
  }

}
