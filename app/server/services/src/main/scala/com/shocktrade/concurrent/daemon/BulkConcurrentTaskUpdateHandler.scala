package com.shocktrade.concurrent.daemon

import com.shocktrade.concurrent.daemon.DaemonUpdateStats.DaemonUpdateOutcome
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentTaskHandler}

/**
  * Bulk Task Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkConcurrentTaskUpdateHandler[IN](expectedBatches: Int) extends ConcurrentTaskHandler[IN, DaemonUpdateOutcome, DaemonUpdateStats] {
  private val status = new DaemonUpdateStats(expectedBatches)

  override def onSuccess(ctx: ConcurrentContext, outcome: DaemonUpdateOutcome) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext, cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext) = status

}