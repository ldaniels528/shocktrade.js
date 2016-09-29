package com.shocktrade.concurrent

/**
  * Concurrent Context - maintains the state for the queue
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConcurrentContext() {
  private[concurrent] var active: Int = 0
  private[concurrent] var completed: Boolean = false
  private var paused: Boolean = false

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
