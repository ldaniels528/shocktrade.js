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
    * @param items       the given input [[js.Array items]]
    * @param handler     the given [[TaskHandler task handler]]
    * @param concurrency the number of concurrent processes to use
    * @return the promise of a result
    */
  def start[IN, OUT, RESULT](items: js.Array[IN], handler: TaskHandler[IN, OUT, RESULT], concurrency: Int = 1)(implicit ec: ExecutionContext) = {
    val promise = Promise[RESULT]()
    val ctx = new ConcurrentContext(items)

    // create a proxy wrapper around the user's handler so that we can intercept the onComplete event
    val proxy = new TaskHandler[IN, OUT, RESULT] {
      override def onNext(ctx: ConcurrentContext[IN], item: IN) = handler.onNext(ctx, item)

      override def onSuccess(ctx: ConcurrentContext[IN], result: OUT) = handler.onSuccess(ctx, result)

      override def onFailure(ctx: ConcurrentContext[IN], cause: Throwable) = handler.onFailure(ctx, cause)

      override def onComplete(ctx: ConcurrentContext[IN]) = {
        val result = handler.onComplete(ctx)
        promise.success(result)
        result
      }
    }

    // schedule the handlers
    (0 to concurrency) foreach (_ => scheduleNext(ctx, proxy))
    promise.future
  }

  /**
    * Manages the asynchronous processing all of items in the queue
    * @param ctx     the given [[ConcurrentContext processing context]]
    * @param handler the given [[TaskHandler task handler]]
    */
  private def handleTask[IN, OUT, RESULT](ctx: ConcurrentContext[IN], handler: TaskHandler[IN, OUT, RESULT])(implicit ec: ExecutionContext): Unit = {
    val anItem = if (ctx.queue.nonEmpty) Option(ctx.queue.pop()) else None
    anItem match {
      case Some(item) =>
        ctx.active += 1
        handler.onNext(ctx, item) onComplete {
          case Success(result) =>
            handler.onSuccess(ctx, result)
            ctx.active -= 1
            scheduleNext(ctx, handler)
          case Failure(e) =>
            handler.onFailure(ctx, e)
            ctx.active -= 1
            scheduleNext(ctx, handler)
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
    * @param handler the given [[TaskHandler task handler]]
    */
  protected def scheduleNext[IN, OUT, RESULT](ctx: ConcurrentContext[IN], handler: TaskHandler[IN, OUT, RESULT])(implicit ec: ExecutionContext): Unit = {
    if (ctx.paused)
      setTimeout(() => scheduleNext(ctx, handler), 1.seconds)
    else
      setImmediate(() => handleTask(ctx, handler))
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
  class ConcurrentContext[IN](val queue: js.Array[IN]) {
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
    * Represents a task handler
    */
  trait TaskHandler[IN, OUT, RESULT] {

    def onNext(ctx: ConcurrentContext[IN], item: IN): Future[OUT]

    def onSuccess(ctx: ConcurrentContext[IN], result: OUT): Any

    def onFailure(ctx: ConcurrentContext[IN], cause: Throwable): Any

    def onComplete(ctx: ConcurrentContext[IN]): RESULT

  }

}