package com.shocktrade.controlpanel.runtime

import org.scalajs.nodejs.NodeRequire

/**
  * Runtime Context
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuntimeContext(val require: NodeRequire)(shutdownHook: => Unit) {
  private var alive = true

  def halt() = {
    alive = false
    shutdownHook
  }

}