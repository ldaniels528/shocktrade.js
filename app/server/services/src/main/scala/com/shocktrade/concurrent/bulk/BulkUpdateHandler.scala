package com.shocktrade.concurrent.bulk

import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentTaskHandler}

import scala.language.implicitConversions

/**
  * Bulk Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkUpdateHandler[IN](expectedBatches: Int) extends ConcurrentTaskHandler[IN, BulkUpdateOutcome, BulkUpdateStatistics] {
  private val status = new BulkUpdateStatistics(expectedBatches)

  override def onSuccess(ctx: ConcurrentContext, outcome: BulkUpdateOutcome) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext, cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext) = status

}
