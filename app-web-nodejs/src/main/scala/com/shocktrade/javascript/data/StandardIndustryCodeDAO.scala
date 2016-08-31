package com.shocktrade.javascript.data

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Standard Industry Code DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait StandardIndustryCodeDAO extends Collection

/**
  * Standard Industry Code DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object StandardIndustryCodeDAO {

  /**
    * Standard Industry Code DAO Extensions
    * @param sicDAO the given [[StandardIndustryCodeDAO SIC DAO]]
    */
  implicit class SICSDAOExtensions(val sicDAO: StandardIndustryCodeDAO) {

    /**
      * Retrieves the Standard Industry Code object for the given SIC code
      * @param sicCode the given SIC code
      */
    @inline
    def findByCode(sicCode: Int)(implicit ec: ExecutionContext) = sicDAO.findOneFuture[NAICS]("sicNumber" $eq sicCode)

  }

  /**
    * Standard Industry Code DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class SICDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getStandardIndustryCodeDAO(implicit ec: ExecutionContext) = db.collectionFuture("SIC").mapTo[StandardIndustryCodeDAO]

  }

}
