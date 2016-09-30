package com.shocktrade.concurrent.bulk

/**
  * Bulk Update Outcome
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class BulkUpdateOutcome(nInserted: Int = 0, nMatched: Int = 0, nModified: Int = 0, nUpserted: Int = 0)
