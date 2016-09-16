package com.shocktrade.common.dao.quotes

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * North American Industry Classification System (NAICS) DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NAICSDAO extends Collection

/**
  * NAICS DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NAICSDAO {

  /**
    * NAICS DAO Extensions
    * @param naicsDAO the given [[NAICSDAO NAICS DAO]]
    */
  implicit class NAICSDAOExtensions(val naicsDAO: NAICSDAO) {

    /**
      * Retrieves the NAICS object for the given NAICS code
      * @param naicsCode the given NAICS code
      */
    @inline
    def findByCode(naicsCode: Int)(implicit ec: ExecutionContext) = naicsDAO.findOneFuture[NAICS]("naicsNumber" $eq naicsCode)

  }

  /**
    * NAICS DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class NAICSDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getNAICSDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("NAICS").mapTo[NAICSDAO]
    }

  }

}