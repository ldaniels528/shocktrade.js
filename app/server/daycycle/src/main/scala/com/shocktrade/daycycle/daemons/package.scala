package com.shocktrade.daycycle

import com.shocktrade.concurrent.daemon.ConcurrentUpdateStatistics.BulkUpdateOutcome
import org.scalajs.nodejs.mongodb.BulkWriteOpResultObject

import scala.language.implicitConversions

/**
  * daemons package object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
package object daemons {

  /**
    * Converts the MongoDB bulk write operation result into a bulk update outcome
    * @param result the given [[BulkWriteOpResultObject bulk write operation result]]
    */
  implicit class BulkWriteOpExtensions(val result: BulkWriteOpResultObject) extends AnyVal {

    /**
      * Converts the MongoDB bulk write operation result into a bulk update outcome
      * @return a [[BulkUpdateOutcome bulk update outcome]]
      */
    @inline
    def toBulkWrite = {
      BulkUpdateOutcome(nInserted = result.nInserted, nMatched = result.nMatched, nModified = result.nModified, nUpserted = result.nUpserted)
    }
  }

}
