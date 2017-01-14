package com.shocktrade.server.dao

import io.scalajs.npm.mongodb.{Collection, Db, MongoClient, MongoDB}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.Try

/**
  * Database Connection Pool
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class DbPool[T <: Collection](dbConnectionString: String, ttl: FiniteDuration = 1.hour)(create: Db => Future[T]) {
  private var dbFuture: Future[Db] = _
  private var lastUpdatedTime: Double = _
  private var dao: Future[T] = _

  def apply()(implicit ec: ExecutionContext): Future[T] = {
    if (dbFuture == null || js.Date.now() - lastUpdatedTime >= ttl.toMillis) {
      Try(Option(dbFuture).foreach(_.foreach(_.close())))
      dbFuture = MongoClient.connectFuture(dbConnectionString)
      dao = dbFuture flatMap create
      lastUpdatedTime = js.Date.now()
    }
    dao
  }

}
