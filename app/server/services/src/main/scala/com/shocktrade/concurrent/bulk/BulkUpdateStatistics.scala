package com.shocktrade.concurrent.bulk

import com.shocktrade.services.LoggerFactory
import org.scalajs.nodejs.duration2Int

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Represents the concurrent processing statistics
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BulkUpdateStatistics(expectedPages: Int) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var nPages = 0
  private var successes = 0
  private var failures = 0
  private var lastUpdated = js.Date.now()

  private var nInserted = 0
  private var nMatched = 0
  private var nModified = 0
  private var nUpserted = 0

  def completion = 100.0 * nPages / expectedPages

  def failed(cause: Throwable) = failures += 1

  def update(outcome: BulkUpdateOutcome) {
    val result = outcome
    val nWritten = result.nInserted + result.nUpserted + result.nModified

    // adjust the detail-level persistence info
    nInserted += result.nInserted
    nMatched += result.nMatched
    nModified += result.nModified
    nUpserted += result.nUpserted

    // update the coarse-grained info
    nPages += 1
    successes += nWritten

    // every 5 seconds, report the progress
    if (js.Date.now() - lastUpdated >= 5.seconds) {
      lastUpdated = js.Date.now()
      logger.info(this.toString)

      // reset the counters
      nInserted = 0
      nMatched = 0
      nModified = 0
      nUpserted = 0
    }
  }

  override def toString = {
    "Processed %d pages (%.01f%%), successes: %d, failures: %d [inserted: %d, matched: %d, modified: %d, upserted: %d]".format(
      nPages, completion, successes, failures, nInserted, nMatched, nModified, nUpserted)
  }
}
