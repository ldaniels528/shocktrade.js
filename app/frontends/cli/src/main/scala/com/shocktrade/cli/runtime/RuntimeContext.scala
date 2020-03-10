package com.shocktrade.cli.runtime

/**
 * Runtime Context
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RuntimeContext(shutdownHook: => Unit) {
  protected var alive = true

  def halt(): Unit = {
    alive = false
    shutdownHook
  }

}