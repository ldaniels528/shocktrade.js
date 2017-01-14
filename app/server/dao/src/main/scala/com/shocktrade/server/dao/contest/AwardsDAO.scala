package com.shocktrade.server.dao.contest

import io.scalajs.npm.mongodb.{Collection, Db}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Awards DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait AwardsDAO extends Collection

/**
  * Awards DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object AwardsDAO {

  /**
    * Awards DAO Extensions
    * @param dao the given [[AwardsDAO Awards DAO]]
    */
  implicit class AwardsDAOExtensions(val dao: AwardsDAO) {

    @inline
    def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]] = {
      dao.find().toArrayFuture[AwardData]
    }

  }

  /**
    * Awards DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class AwardsDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getAwardsDAO(implicit ec: ExecutionContext): Future[AwardsDAO] = {
      db.collectionFuture("Awards").mapTo[AwardsDAO]
    }
  }

}