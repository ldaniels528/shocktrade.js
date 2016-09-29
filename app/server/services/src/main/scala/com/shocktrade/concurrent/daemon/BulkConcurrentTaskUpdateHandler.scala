package com.shocktrade.concurrent.daemon

import com.shocktrade.concurrent.daemon.ConcurrentUpdateStatistics.DaemonUpdateOutcome
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentTaskHandler}

import scala.language.implicitConversions

/**
  * Bulk Concurrent Task Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkConcurrentTaskUpdateHandler[IN](expectedBatches: Int) extends ConcurrentTaskHandler[IN, DaemonUpdateOutcome, ConcurrentUpdateStatistics] {
  private val status = new ConcurrentUpdateStatistics(expectedBatches)

  override def onSuccess(ctx: ConcurrentContext, outcome: DaemonUpdateOutcome) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext, cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext) = status

}
