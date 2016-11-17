package com.shocktrade.controlpanel.runtime

/**
  * Runtime Context
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuntimeContext {
  private var alive = true
  private val variables = Map[String, Variable]()

  def findVariable(name: String) = variables.get(name)

  def halt(exitCode: Int = 0) = {
    alive = false
    Option(exitCode)
  }

}
