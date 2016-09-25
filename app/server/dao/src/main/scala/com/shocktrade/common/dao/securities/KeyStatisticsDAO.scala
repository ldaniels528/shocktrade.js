package com.shocktrade.common.dao.securities

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Key Statistics DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait KeyStatisticsDAO extends Collection

/**
  * Key Statistics DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object KeyStatisticsDAO {

  /**
    * Key Statistics DAO Enrichment
    * @param dao the given [[KeyStatisticsDAO Key Statistics DAO]]
    */
  implicit class KeyStatisticsDAOEnrichment(val dao: KeyStatisticsDAO) extends AnyVal {

    /**
      * Retrieves the key statistics for the given symbol
      * @param symbol the given symbol (e.g. "AAPL")
      * @return the promise of an option of [[KeyStatisticsData key statistics]] data object
      */
    @inline
    def findBySymbol(symbol: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[KeyStatisticsData]("symbol" $eq symbol)
    }

    /**
      * Upserts the given key statistics data object
      * @param ks the given [[KeyStatisticsData key statistics]] data object
      * @return the promise of an [[UpdateWriteOpResultObject update result]]
      */
    @inline
    def saveKeyStatistics(ks: KeyStatisticsData)(implicit ec: ExecutionContext) = {
      dao.updateOne(
        filter = "symbol" $eq ks.symbol,
        update = ks,
        options = new UpdateOptions(upsert = true)
      ).toFuture
    }

  }

  /**
    * Key Statistics DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class KeyStatisticsDAOConstructors(val db: Db) extends AnyVal {

    /**
      * Retrieves the Key Statistics DAO instance
      * @return the [[KeyStatisticsDAO Key Statistics DAO]] instance
      */
    @inline
    def getKeyStatisticsDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("KeyStatistics").mapTo[KeyStatisticsDAO]
    }

  }
  
}