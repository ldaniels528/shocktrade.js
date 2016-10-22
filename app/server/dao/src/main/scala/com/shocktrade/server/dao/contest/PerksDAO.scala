package com.shocktrade.server.dao.contest

import org.scalajs.nodejs.mongodb.{Collection, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Perks DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait PerksDAO extends Collection

/**
  * Perks DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PerksDAO {

  /**
    * Perks DAO Extensions
    * @param dao the given [[PerksDAO Perks DAO]]
    */
  implicit class PerksDAOExtensions(val dao: PerksDAO) {

    @inline
    def findAvailablePerks(implicit ec: ExecutionContext) = {
      dao.find().toArrayFuture[PerkData]
    }

  }
  
  /**
    * Perks DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class PerksDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getPerksDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Perks").mapTo[PerksDAO]
    }
  }

}