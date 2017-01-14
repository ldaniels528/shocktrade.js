package com.shocktrade.controlpanel.runtime

/**
  * Runtime Context
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuntimeContext(shutdownHook: => Unit) {
  private var alive = true

  def halt() = {
    alive = false
    shutdownHook
  }

}