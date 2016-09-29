package com.shocktrade.concurrent.daemon

import com.shocktrade.concurrent.daemon.BulkUpdateStatistics.DaemonUpdateOutcome
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentTaskHandler}

import scala.language.implicitConversions

/**
  * Bulk Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkUpdateHandler[IN](expectedBatches: Int) extends ConcurrentTaskHandler[IN, DaemonUpdateOutcome, BulkUpdateStatistics] {
  private val status = new BulkUpdateStatistics(expectedBatches)

  override def onSuccess(ctx: ConcurrentContext, outcome: DaemonUpdateOutcome) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext, cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext) = status

}
