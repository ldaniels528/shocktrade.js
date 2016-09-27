package com.shocktrade.concurrent.daemon

import com.shocktrade.concurrent.ConcurrentProcessor.{ConcurrentContext, TaskHandler}
import com.shocktrade.concurrent.daemon.DaemonUpdateStats.DaemonUpdateOutcome

import scala.concurrent.Future

/**
  * Bulk Task Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkTaskUpdateHandler[IN](expectedBatches: Int) extends TaskHandler[IN, DaemonUpdateOutcome, DaemonUpdateStats] {
  private val status = new DaemonUpdateStats(expectedBatches)

  override def onNext(ctx: ConcurrentContext[IN], input: IN) = processBatch(input)

  override def onSuccess(ctx: ConcurrentContext[IN], outcome: DaemonUpdateOutcome) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext[IN], cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext[IN]) = status

  def processBatch(input: IN): Future[DaemonUpdateOutcome]

}