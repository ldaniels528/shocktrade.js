package com.shocktrade.concurrent.bulk

import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentTaskHandler}
import com.shocktrade.serverside.LoggerFactory.Logger

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
  * Bulk Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
abstract class BulkUpdateHandler[IN](expectedBatches: Int, reportInterval: FiniteDuration = 5.seconds)
  extends ConcurrentTaskHandler[IN, BulkUpdateOutcome, BulkUpdateStatistics] {
  private val status = new BulkUpdateStatistics(expectedBatches, reportInterval)

  override def onSuccess(ctx: ConcurrentContext, outcome: BulkUpdateOutcome)(implicit logger: Logger) = status.update(outcome)

  override def onFailure(ctx: ConcurrentContext, cause: Throwable) = status.failed(cause)

  override def onComplete(ctx: ConcurrentContext) = status

}
