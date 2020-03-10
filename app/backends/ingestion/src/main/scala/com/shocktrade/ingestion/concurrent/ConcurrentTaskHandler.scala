package com.shocktrade.ingestion.concurrent

import com.shocktrade.server.common.LoggerFactory.Logger

import scala.concurrent.Future

/**
 * Represents a concurrent task handler
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ConcurrentTaskHandler[IN, OUT, SUMMARY] {

  def onNext(ctx: ConcurrentContext, item: IN): Future[OUT]

  def onSuccess(ctx: ConcurrentContext, outcome: OUT)(implicit logger: Logger): Unit

  def onFailure(ctx: ConcurrentContext, cause: Throwable): Unit

  def onComplete(ctx: ConcurrentContext): SUMMARY

}

