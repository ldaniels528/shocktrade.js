package com.shocktrade.daycycle

import org.scalajs.nodejs.mongodb.BulkWriteOpResultObject

import scala.scalajs.js

/**
  * Represents the result of a batch operation
  * @param written the number of records written or updated
  * @param errors  any errors that occurred
  */
class BatchResult(val written: Int, val errors: js.Array[String] = js.Array()) {

  def +(batchResult: BatchResult) = {
    new BatchResult(written = batchResult.written + written, errors = errors ++ batchResult.errors)
  }

}

/**
  * Batch Result Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BatchResult {

  def apply(result: BulkWriteOpResultObject) = {
    new BatchResult(written = result.nInserted + result.nUpserted + result.nModified)
  }

}