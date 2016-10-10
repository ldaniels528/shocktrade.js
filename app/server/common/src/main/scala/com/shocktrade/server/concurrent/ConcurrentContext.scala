package com.shocktrade.server.concurrent

/**
  * Concurrent Context - maintains the state for the queue
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait ConcurrentContext {
  private[concurrent] var active: Int = 0
  private[concurrent] var completed: Boolean = false
  private var paused: Boolean = false

  /**
    * @return the number of concurrent processes to use
    */
  def concurrency: Int

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
  * Concurrent Context
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ConcurrentContext {

  /**
    * Returns the default instance of a concurrent context
    * @param concurrency the number of concurrent processes to use
    * @return the default instance of a [[ConcurrentContext concurrent context]]
    */
  def apply(concurrency: Int = 1) = new BasicConcurrentContext(concurrency)

  /**
    * Basic Concurrent Context
    * @param concurrency the number of concurrent processes to use
    */
  class BasicConcurrentContext(val concurrency: Int = 1) extends ConcurrentContext

}