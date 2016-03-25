package com.shocktrade.services

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{Success, Failure }

/**
 * Resource Throttle
 * @author lawrence.daniels@gmail.com
 */
trait Throttle[T] {
  protected def logger: org.slf4j.Logger
  protected val outstandingItems = new TrieMap[T, Future[_]]()

  def getNextItem(items: Iterator[T], maxItems: Int = 5, timeOutMillis: Long = 15000L): T = {
    val timeOut = System.currentTimeMillis() + timeOutMillis
    val wasFull = outstandingItems.size >= maxItems
    while (System.currentTimeMillis() < timeOut && items.hasNext && outstandingItems.size >= 5) {
      logger.info(s"Maximum resources used (count = ${outstandingItems.size})")
      Thread.sleep(1000)
    }
    if (wasFull) {
      logger.info(s"Resources freed (count = ${outstandingItems.size})")
    }
    items.next()
  }

  def getNextItems(items: Iterator[T], count: Int, maxRequests: Int = 5, timeOutMillis: Long = 15000L): Iterator[T] = {
    val timeOut = System.currentTimeMillis() + timeOutMillis
    val wasFull = outstandingItems.size >= maxRequests
    while (System.currentTimeMillis() < timeOut && items.hasNext && outstandingItems.size >= 5) {
      logger.info(s"Maximum resources used (count = ${outstandingItems.size})")
      Thread.sleep(1000)
    }
    if (wasFull) {
      logger.info(s"Resources freed (count = ${outstandingItems.size})")
    }
    items.take(count)
  }

  def watch[S](item: T, task: Future[S])(implicit ec: ExecutionContext): Future[S] = {
    outstandingItems += (item -> task)
    task.onComplete {
      case Success(result) => outstandingItems -= item
      case Failure(e) => outstandingItems -= item
    }
    task
  }

}
