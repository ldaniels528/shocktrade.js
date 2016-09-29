package com.shocktrade.concurrent

import com.shocktrade.concurrent.ConcurrentProcessor._
import org.scalajs.nodejs.{duration2Int, setImmediate, setTimeout}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Concurrent Processor
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConcurrentProcessor() {

  /**
    * Starts the concurrent processor. 
    * <b>NOTE</b>: For performance reasons the input items are used directly for processing.
    * @param queue       the given input [[js.Array queue]]
    * @param handler     the given [[ConcurrentTaskHandler task handler]]
    * @param concurrency the number of concurrent processes to use
    * @return the promise of a result
    */
  def start[IN, OUT, SUMMARY](queue: js.Array[IN], handler: ConcurrentTaskHandler[IN, OUT, SUMMARY], concurrency: Int = 1)(implicit ec: ExecutionContext) = {
    val promise = Promise[SUMMARY]()
    val ctx = new ConcurrentContext()

    // create a proxy wrapper around the user's handler so that we can intercept the onComplete event
    val proxy = new ConcurrentTaskHandler[IN, OUT, SUMMARY] {
      override def onNext(ctx: ConcurrentContext, item: IN) = handler.onNext(ctx, item)

      override def onSuccess(ctx: ConcurrentContext, result: OUT) = handler.onSuccess(ctx, result)

      override def onFailure(ctx: ConcurrentContext, cause: Throwable) = handler.onFailure(ctx, cause)

      override def onComplete(ctx: ConcurrentContext) = {
        val result = handler.onComplete(ctx)
        promise.success(result)
        result
      }
    }

    // schedule the handlers
    (0 to concurrency) foreach (_ => scheduleNext(queue, ctx, proxy))
    promise.future
  }

  /**
    * Manages the asynchronous processing all of items in the queue
    * @param ctx     the given [[ConcurrentContext processing context]]
    * @param handler the given [[ConcurrentTaskHandler task handler]]
    */
  private def handleTask[IN, OUT, RESULT](queue: js.Array[IN], ctx: ConcurrentContext, handler: ConcurrentTaskHandler[IN, OUT, RESULT])(implicit ec: ExecutionContext): Unit = {
    val anItem = if (queue.nonEmpty) Option(queue.pop()) else None
    anItem match {
      case Some(item) =>
        ctx.active += 1
        handler.onNext(ctx, item) onComplete {
          case Success(result) =>
            handler.onSuccess(ctx, result)
            ctx.active -= 1
            scheduleNext(queue, ctx, handler)
          case Failure(e) =>
            handler.onFailure(ctx, e)
            ctx.active -= 1
            scheduleNext(queue, ctx, handler)
        }
      case None =>
        if (!ctx.completed && ctx.active == 0) {
          ctx.completed = true
          handler.onComplete(ctx)
        }
    }
  }

  /**
    * Schedules the next item in the queue for processing
    * @param ctx     the given [[ConcurrentContext processing context]]
    * @param handler the given [[ConcurrentTaskHandler task handler]]
    */
  private def scheduleNext[IN, OUT, SUMMARY](queue: js.Array[IN], ctx: ConcurrentContext, handler: ConcurrentTaskHandler[IN, OUT, SUMMARY])(implicit ec: ExecutionContext): Unit = {
    if (ctx.paused)
      setTimeout(() => scheduleNext(queue, ctx, handler), 1.seconds)
    else
      setImmediate(() => handleTask(queue, ctx, handler))
  }

}

/**
  * Batching Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ConcurrentProcessor {

  /**
    * Concurrent Context - maintains the state for the queue
    */
  class ConcurrentContext() {
    private[concurrent] var active: Int = 0
    private[concurrent] var completed: Boolean = false
    private[concurrent] var paused: Boolean = false

    /**
      * @return true, if processing is currently paused
      */
    def isPaused = paused

    /**
      * Pauses the process
      */
    def pause(): Unit = if (!completed) paused = true

    /**
      * If the process is paused, execution is resumed
      */
    def resume(): Unit = paused = false

  }

  /**
    * Represents a concurrent task handler
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  trait ConcurrentTaskHandler[IN, OUT, SUMMARY] {

    def onNext(ctx: ConcurrentContext, item: IN): Future[OUT]

    def onSuccess(ctx: ConcurrentContext, outcome: OUT): Any

    def onFailure(ctx: ConcurrentContext, cause: Throwable): Any

    def onComplete(ctx: ConcurrentContext): SUMMARY

  }

}