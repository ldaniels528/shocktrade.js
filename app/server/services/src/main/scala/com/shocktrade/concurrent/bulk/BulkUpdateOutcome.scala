package com.shocktrade.concurrent.bulk

import org.scalajs.nodejs.mongodb.{BulkWriteOpResultObject, UpdateWriteOpResultObject}

/**
  * Bulk Update Outcome
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class BulkUpdateOutcome(nInserted: Int = 0, nMatched: Int = 0, nModified: Int = 0, nUpserted: Int = 0, nFailures: Int = 0)

/**
  * Bulk Update Outcome Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BulkUpdateOutcome {

  /**
    * Bulk Update Outcome Extensions
    * @param outcome the given [[BulkUpdateOutcome outcome]]
    */
  implicit class BulkUpdateOutcomeExtensions(val outcome: BulkUpdateOutcome) extends AnyVal {

    @inline
    def +(other: BulkUpdateOutcome) = {
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
  implicit class BulkWriteOpResultObjectExtensions(val result: BulkWriteOpResultObject) extends AnyVal {

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

  /**
    * Bulk Write-Op Result Extensions
    * @param result the given [[BulkWriteOpResultObject bulk write operation result]]
    */
  implicit class UpdateWriteOpResultObjectExtensions(val result: UpdateWriteOpResultObject) extends AnyVal {

    /**
      * Converts the MongoDB bulk write operation result into a bulk update outcome
      * @return a [[BulkUpdateOutcome bulk update outcome]]
      */
    @inline
    def toBulkWrite = {
      BulkUpdateOutcome(
        nInserted = 0,
        nMatched = 0,
        nModified = result.result.nModified,
        nUpserted = 0
      )
    }
  }

}
