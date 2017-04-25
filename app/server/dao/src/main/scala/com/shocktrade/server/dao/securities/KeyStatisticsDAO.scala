package com.shocktrade.server.dao.securities

import io.scalajs.npm.mongodb._

import scala.concurrent.{ExecutionContext, Future}
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
    def findBySymbol(symbol: String)(implicit ec: ExecutionContext): Future[Option[KeyStatisticsData]] = {
      dao.findOneFuture[KeyStatisticsData]("symbol" $eq symbol)
    }

    /**
      * Upserts the given key statistics data object
      * @param keyStats the given collection of [[KeyStatisticsData key statistics]] data objects
      * @return the promise of an [[BulkWriteOpResultObject bulk update result]]
      */
    @inline
    def saveKeyStatistics(keyStats: KeyStatisticsData*): js.Promise[BulkWriteOpResultObject] = {
      dao.bulkWrite(
        js.Array(keyStats map (ks => updateOne(
          filter = "symbol" $eq ks.symbol,
          update = ks,
          upsert = true
        )): _*))
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
    def getKeyStatisticsDAO: KeyStatisticsDAO = {
      db.collection("KeyStatistics").asInstanceOf[KeyStatisticsDAO]
    }

  }

}