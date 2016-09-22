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

    @inline
    def getKeyStatisticsDAO(implicit ec: ExecutionContext) = db.collectionFuture("KeyStatistics").mapTo[KeyStatisticsDAO]

  }
  
}