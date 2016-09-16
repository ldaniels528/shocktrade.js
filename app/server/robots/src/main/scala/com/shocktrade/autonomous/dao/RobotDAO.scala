package com.shocktrade.autonomous.dao

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Robot DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait RobotDAO extends Collection

/**
  * Robot DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RobotDAO {

  /**
    * Robot DAO Extensions
    * @param dao the given [[RobotDAO Robot DAO]]
    */
  implicit class RobotDAOExtensions(val dao: RobotDAO) {

    /**
      * Retrieves an array of robots
      * @return an array of [[RobotData robots]]
      */
    @inline
    def findRobots()(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.find("active" $eq true).sort(js.Array("lastActivated", 1)).toArrayFuture[RobotData]
    }

  }

  /**
    * Robot DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class RobotDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getRobotDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Robots").mapTo[RobotDAO]
    }
  }

}