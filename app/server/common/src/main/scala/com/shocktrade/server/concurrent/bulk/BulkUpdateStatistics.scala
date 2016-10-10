package com.shocktrade.server.concurrent.bulk

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.LoggerFactory.Logger
import org.scalajs.nodejs.duration2Int

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Represents the concurrent processing statistics
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BulkUpdateStatistics(expectedPages: Int, reportInterval: FiniteDuration = 5.seconds) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var nPages = 0
  private var successes = 0
  private var failures = 0
  private var lastUpdated = js.Date.now()

  private var nInserted = 0
  private var nMatched = 0
  private var nModified = 0
  private var nUpserted = 0
  private var nFailures = 0

  def completion = 100.0 * nPages / expectedPages

  def failed(cause: Throwable) = failures += 1

  def update(result: BulkUpdateOutcome)(implicit logger: Logger) {
    // adjust the detail-level persistence info
    nInserted += result.nInserted
    nMatched += result.nMatched
    nModified += result.nModified
    nUpserted += result.nUpserted
    nFailures += result.nFailures

    // update the coarse-grained info
    nPages += 1
    successes += result.nInserted + result.nUpserted + result.nModified
    failures += result.nFailures

    // every 5 seconds, report the progress
    if (js.Date.now() - lastUpdated >= reportInterval) {
      lastUpdated = js.Date.now()
      logger.info(this.toString)

      // reset the counters
      nInserted = 0
      nMatched = 0
      nModified = 0
      nUpserted = 0
      nFailures = 0
    }
  }

  override def toString = {
    "Processed %d pages (%.01f%%), successes: %d, failures: %d [inserted: %d, matched: %d, modified: %d, upserted: %d, failures: %d]".format(
      nPages, completion, successes, failures, nInserted, nMatched, nModified, nUpserted, nFailures)
  }
}
