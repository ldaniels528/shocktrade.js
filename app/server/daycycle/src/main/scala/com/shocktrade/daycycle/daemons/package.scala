package com.shocktrade.daycycle

import com.shocktrade.concurrent.daemon.BulkUpdateStatistics.BulkUpdateOutcome
import org.scalajs.nodejs.mongodb.BulkWriteOpResultObject

import scala.language.implicitConversions

/**
  * daemons package object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
package object daemons {

  /**
    * Bulk Update Outcome Extensions
    * @param outcome the given [[BulkUpdateOutcome outcome]]
    */
  implicit class BulkUpdateOutcomeExtensions(val outcome: BulkUpdateOutcome) extends AnyVal {

    @inline
    def ++(other: BulkUpdateOutcome) = {
      BulkUpdateOutcome(
        nInserted = outcome.nInserted + other.nInserted,
        nMatched = outcome.nMatched + other.nMatched,
        nModified = outcome.nModified + other.nModified,
        nUpserted = outcome.nUpserted + other.nUpserted
      )
    }

  }

  /**
    * Bulk Write-Op Result Extensions
    * @param result the given [[BulkWriteOpResultObject bulk write operation result]]
    */
  implicit class BulkWriteOpExtensions(val result: BulkWriteOpResultObject) extends AnyVal {

    /**
      * Converts the MongoDB bulk write operation result into a bulk update outcome
      * @return a [[BulkUpdateOutcome bulk update outcome]]
      */
    @inline
    def toBulkWrite = {
      BulkUpdateOutcome(
        nInserted = result.nInserted,
        nMatched = result.nMatched,
        nModified = result.nModified,
        nUpserted = result.nUpserted
      )
    }

  }

}
