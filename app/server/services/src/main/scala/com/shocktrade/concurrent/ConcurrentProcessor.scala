package com.shocktrade.concurrent

import com.shocktrade.serverside.LoggerFactory.Logger
import org.scalajs.nodejs.{duration2Int, setImmediate, setTimeout}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Concurrent Processor
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConcurrentProcessor {

  /**
    * Starts the concurrent processor. 
    * <b>NOTE</b>: For performance reasons the input items are used directly for processing.
    * @param queue   the given [[js.Array input queue]]
    * @param ctx     the given [[ConcurrentContext concurrent context]]
    * @param handler the given [[ConcurrentTaskHandler concurrent task handler]]
    * @return the promise of a result
    */
  def start[IN, OUT, SUMMARY](queue: js.Array[IN],
                              ctx: ConcurrentContext = ConcurrentContext(concurrency = 1),
                              handler: ConcurrentTaskHandler[IN, OUT, SUMMARY])(implicit ec: ExecutionContext, logger: Logger) = {
    val promise = Promise[SUMMARY]()

    // create a proxy wrapper around the user's handler so that we can intercept the onComplete event
    val proxy = new ConcurrentTaskHandler[IN, OUT, SUMMARY] {
      override def onNext(ctx: ConcurrentContext, item: IN) = handler.onNext(ctx, item)

      override def onSuccess(ctx: ConcurrentContext, outcome: OUT)(implicit logger: Logger) = handler.onSuccess(ctx, outcome)

      override def onFailure(ctx: ConcurrentContext, cause: Throwable) = handler.onFailure(ctx, cause)

      override def onComplete(ctx: ConcurrentContext) = {
        val result = handler.onComplete(ctx)
        promise.success(result)
        result
      }
    }

    // schedule the handlers
    (0 to ctx.concurrency) foreach (_ => scheduleNext(queue, ctx, proxy))
    promise.future
  }

  /**
    * Manages the asynchronous processing all of items in the queue
    * @param ctx     the given [[ConcurrentContext processing context]]
    * @param handler the given [[ConcurrentTaskHandler task handler]]
    */
  private def handleTask[IN, OUT, RESULT](queue: js.Array[IN], ctx: ConcurrentContext, handler: ConcurrentTaskHandler[IN, OUT, RESULT])(implicit ec: ExecutionContext, logger: Logger): Unit = {
    val anItem = if (queue.nonEmpty) Option(queue.pop()) else None
    anItem match {
      case Some(item) =>
        ctx.active += 1
        handler.onNext(ctx, item) onComplete {
          case Success(outcome) =>
            handler.onSuccess(ctx, outcome)
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
  private def scheduleNext[IN, OUT, SUMMARY](queue: js.Array[IN], ctx: ConcurrentContext, handler: ConcurrentTaskHandler[IN, OUT, SUMMARY])(implicit ec: ExecutionContext, logger: Logger): Unit = {
    if (ctx.isPaused)
      setTimeout(() => scheduleNext(queue, ctx, handler), 1.seconds)
    else
      setImmediate(() => handleTask(queue, ctx, handler))
  }

}
