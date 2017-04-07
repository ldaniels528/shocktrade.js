package com.shocktrade.server.dao.contest

import io.scalajs.npm.mongodb.{Collection, Db}

import scala.concurrent.ExecutionContext
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
    def findAvailableAwards(implicit ec: ExecutionContext): js.Promise[js.Array[AwardData]] = {
      dao.find[AwardData]().toArray()
    }

  }

  /**
    * Awards DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class AwardsDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getAwardsDAO: AwardsDAO = {
      db.collection("Awards").asInstanceOf[AwardsDAO]
    }
  }

}