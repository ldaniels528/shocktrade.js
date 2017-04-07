package com.shocktrade.server.dao.contest

import io.scalajs.npm.mongodb.{Collection, Db}

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
    def findAvailablePerks(implicit ec: ExecutionContext): js.Promise[js.Array[PerkData]] = {
      dao.find[PerkData]().toArray()
    }

  }

  /**
    * Perks DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class PerksDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getPerksDAO: PerksDAO = {
      db.collection("Perks").asInstanceOf[PerksDAO]
    }
  }

}